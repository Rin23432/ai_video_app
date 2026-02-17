package com.animegen.service;

import com.animegen.common.BizException;
import com.animegen.common.ErrorCodes;
import com.animegen.dao.domain.VideoEntitlementDO;
import com.animegen.dao.domain.VideoMallSkuViewDO;
import com.animegen.dao.domain.VideoOrderDO;
import com.animegen.dao.domain.VideoOrderItemDO;
import com.animegen.dao.domain.WorkDO;
import com.animegen.dao.domain.UserDO;
import com.animegen.dao.mapper.VideoEntitlementMapper;
import com.animegen.dao.mapper.VideoOrderItemMapper;
import com.animegen.dao.mapper.VideoOrderMapper;
import com.animegen.dao.mapper.VideoProductSkuMapper;
import com.animegen.dao.mapper.WorkMapper;
import com.animegen.dao.mapper.UserMapper;
import com.animegen.service.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VideoMallService {
    private final VideoProductSkuMapper videoProductSkuMapper;
    private final VideoOrderMapper videoOrderMapper;
    private final VideoOrderItemMapper videoOrderItemMapper;
    private final VideoEntitlementMapper videoEntitlementMapper;
    private final WorkMapper workMapper;
    private final UserMapper userMapper;

    public VideoMallService(VideoProductSkuMapper videoProductSkuMapper,
                            VideoOrderMapper videoOrderMapper,
                            VideoOrderItemMapper videoOrderItemMapper,
                            VideoEntitlementMapper videoEntitlementMapper,
                            WorkMapper workMapper,
                            UserMapper userMapper) {
        this.videoProductSkuMapper = videoProductSkuMapper;
        this.videoOrderMapper = videoOrderMapper;
        this.videoOrderItemMapper = videoOrderItemMapper;
        this.videoEntitlementMapper = videoEntitlementMapper;
        this.workMapper = workMapper;
        this.userMapper = userMapper;
    }

    public VideoMallCatalogListResponse listVideos(String sort, Long cursor, Integer limit) {
        long safeCursor = cursor == null ? 0L : Math.max(cursor, 0L);
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<VideoMallSkuViewDO> rows = videoProductSkuMapper.listOnSaleByLatest(safeCursor, safeLimit);
        List<VideoMallCatalogItemDTO> items = new ArrayList<>();
        for (VideoMallSkuViewDO row : rows) {
            VideoMallCatalogItemDTO dto = new VideoMallCatalogItemDTO();
            dto.setSpuId(row.getSpuId());
            dto.setSkuId(row.getSkuId());
            dto.setWorkId(row.getWorkId());
            dto.setTitle(row.getTitle());
            dto.setCoverUrl(row.getCoverUrl());
            dto.setPriceCent(row.getPriceCent());
            dto.setOriginPriceCent(row.getOriginPriceCent());
            dto.setCurrency(row.getCurrency());
            dto.setStockAvailable(row.getStockAvailable());
            dto.setStatus(row.getStatus());
            items.add(dto);
        }
        Long nextCursor = items.isEmpty() ? safeCursor : items.get(items.size() - 1).getSkuId();
        return new VideoMallCatalogListResponse(items, nextCursor);
    }

    public VideoMallSkuDetailResponse getVideoDetail(Long skuId) {
        VideoMallSkuViewDO row = videoProductSkuMapper.findOnSaleDetail(skuId);
        if (row == null) {
            throw new BizException(ErrorCodes.MALL_SKU_NOT_FOUND, "sku not found");
        }
        VideoMallSkuDetailResponse response = new VideoMallSkuDetailResponse();
        response.setSpuId(row.getSpuId());
        response.setSkuId(row.getSkuId());
        response.setWorkId(row.getWorkId());
        response.setTitle(row.getTitle());
        response.setSubtitle(row.getSubtitle());
        response.setDescription(row.getDescription());
        response.setCoverUrl(row.getCoverUrl());
        response.setPriceCent(row.getPriceCent());
        response.setOriginPriceCent(row.getOriginPriceCent());
        response.setCurrency(row.getCurrency());
        response.setValidDays(row.getValidDays());
        response.setStockAvailable(row.getStockAvailable());
        response.setStatus(row.getStatus());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public VideoMallOrderCreateResponse createOrder(Long userId, VideoMallOrderCreateRequest request) {
        requireInteractiveUser(userId);
        if (request.getItems().size() != 1) {
            throw new BizException(ErrorCodes.INVALID_PARAM, "only one item is supported currently");
        }
        VideoMallOrderCreateRequest.OrderItem item = request.getItems().get(0);
        VideoOrderDO existed = videoOrderMapper.findByUserAndRequestId(userId, request.getRequestId().trim());
        if (existed != null) {
            return new VideoMallOrderCreateResponse(
                    existed.getOrderNo(),
                    existed.getStatus(),
                    existed.getPayAmountCent(),
                    existed.getCurrency(),
                    existed.getPayDeadlineAt()
            );
        }

        VideoMallSkuViewDO sku = videoProductSkuMapper.findOnSaleDetail(item.getSkuId());
        if (sku == null) {
            throw new BizException(ErrorCodes.MALL_SKU_NOT_FOUND, "sku not found");
        }
        int updated = videoProductSkuMapper.decreaseStockAvailable(item.getSkuId(), item.getQuantity());
        if (updated <= 0) {
            throw new BizException(ErrorCodes.MALL_STOCK_NOT_ENOUGH, "stock not enough");
        }

        String orderNo = buildOrderNo();
        LocalDateTime deadline = LocalDateTime.now().plusMinutes(15);
        int payAmount = sku.getPriceCent() * item.getQuantity();

        VideoOrderDO order = new VideoOrderDO();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmountCent(payAmount);
        order.setPayAmountCent(payAmount);
        order.setCurrency(sku.getCurrency());
        order.setStatus("PENDING_PAY");
        order.setPayDeadlineAt(deadline);
        order.setVersion(0);
        order.setRequestId(request.getRequestId().trim());
        videoOrderMapper.insert(order);

        VideoOrderItemDO orderItem = new VideoOrderItemDO();
        orderItem.setOrderNo(orderNo);
        orderItem.setSkuId(sku.getSkuId());
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setWorkId(sku.getWorkId());
        orderItem.setTitleSnapshot(sku.getTitle());
        orderItem.setCoverSnapshot(sku.getCoverUrl());
        orderItem.setUnitPriceCent(sku.getPriceCent());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setAmountCent(payAmount);
        videoOrderItemMapper.insert(orderItem);

        return new VideoMallOrderCreateResponse(orderNo, order.getStatus(), payAmount, order.getCurrency(), deadline);
    }

    public VideoMallOrderListResponse myOrders(Long userId, String status, Long cursor, Integer limit) {
        requireInteractiveUser(userId);
        long safeCursor = cursor == null ? 0L : Math.max(cursor, 0L);
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        String normalized = normalizeStatus(status);
        List<VideoOrderDO> rows = videoOrderMapper.listByUser(userId, normalized, safeCursor, safeLimit);
        List<VideoMallOrderItemDTO> items = new ArrayList<>();
        for (VideoOrderDO row : rows) {
            VideoMallOrderItemDTO dto = new VideoMallOrderItemDTO();
            dto.setOrderNo(row.getOrderNo());
            dto.setStatus(row.getStatus());
            dto.setPayAmountCent(row.getPayAmountCent());
            dto.setCurrency(row.getCurrency());
            dto.setCreatedAt(row.getCreatedAt());
            dto.setPaidAt(row.getPaidAt());
            items.add(dto);
        }
        Long nextCursor = rows.isEmpty() ? safeCursor : rows.get(rows.size() - 1).getId();
        return new VideoMallOrderListResponse(items, nextCursor);
    }

    public VideoMallEntitlementListResponse myEntitlements(Long userId, Long cursor, Integer limit) {
        requireInteractiveUser(userId);
        long safeCursor = cursor == null ? 0L : Math.max(cursor, 0L);
        int safeLimit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
        List<VideoEntitlementDO> rows = videoEntitlementMapper.listByUser(userId, safeCursor, safeLimit);
        List<VideoMallEntitlementItemDTO> items = new ArrayList<>();
        for (VideoEntitlementDO row : rows) {
            VideoMallEntitlementItemDTO dto = new VideoMallEntitlementItemDTO();
            WorkDO work = workMapper.findById(row.getWorkId());
            dto.setWorkId(row.getWorkId());
            dto.setTitle(work == null ? "work-" + row.getWorkId() : work.getTitle());
            dto.setCoverUrl(work == null ? null : work.getCoverUrl());
            dto.setGrantedAt(row.getGrantedAt());
            dto.setExpireAt(row.getExpireAt());
            dto.setStatus(row.getStatus());
            items.add(dto);
        }
        Long nextCursor = rows.isEmpty() ? safeCursor : rows.get(rows.size() - 1).getId();
        return new VideoMallEntitlementListResponse(items, nextCursor);
    }

    @Transactional(rollbackFor = Exception.class)
    public VideoMallPaymentPrepayResponse prepay(Long userId, String orderNo, VideoMallPaymentPrepayRequest request) {
        requireInteractiveUser(userId);
        VideoOrderDO order = requireOrder(userId, orderNo);
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BizException(ErrorCodes.MALL_ORDER_STATUS_INVALID, "order status invalid");
        }
        int changed = videoOrderMapper.markPaid(orderNo);
        if (changed > 0) {
            grantEntitlement(userId, orderNo);
        }
        VideoMallPaymentPrepayResponse response = new VideoMallPaymentPrepayResponse();
        response.setChannel(request.getChannel());
        response.setPrepayToken("mock-prepay-" + UUID.randomUUID());
        response.setExpireAt(LocalDateTime.now().plusMinutes(15));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void paymentCallback(VideoMallPaymentCallbackRequest request) {
        VideoOrderDO order = videoOrderMapper.findByOrderNo(request.getOrderNo());
        if (order == null) {
            throw new BizException(ErrorCodes.MALL_ORDER_NOT_FOUND, "order not found");
        }
        if (!"SUCCESS".equalsIgnoreCase(request.getStatus())) {
            return;
        }
        int changed = videoOrderMapper.markPaid(request.getOrderNo());
        if (changed > 0) {
            grantEntitlement(order.getUserId(), order.getOrderNo());
        }
    }

    public VideoMallPlayAuthResponse playAuth(Long userId, Long workId) {
        requireInteractiveUser(userId);
        VideoEntitlementDO entitlement = videoEntitlementMapper.findActiveByUserAndWork(userId, workId);
        VideoMallPlayAuthResponse response = new VideoMallPlayAuthResponse();
        if (entitlement == null) {
            response.setAllowed(false);
            response.setReason("NO_ENTITLEMENT");
            return response;
        }
        WorkDO work = workMapper.findById(workId);
        if (work == null || work.getVideoUrl() == null || work.getVideoUrl().trim().isEmpty()) {
            response.setAllowed(false);
            response.setReason("VIDEO_NOT_READY");
            return response;
        }
        response.setAllowed(true);
        response.setReason("OK");
        response.setPlayToken("play-" + UUID.randomUUID());
        response.setPlayUrl(work.getVideoUrl());
        response.setExpireAt(LocalDateTime.now().plusMinutes(30));
        return response;
    }

    private VideoOrderDO requireOrder(Long userId, String orderNo) {
        VideoOrderDO order = videoOrderMapper.findByOrderNo(orderNo);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BizException(ErrorCodes.MALL_ORDER_NOT_FOUND, "order not found");
        }
        return order;
    }

    private void grantEntitlement(Long userId, String orderNo) {
        List<VideoOrderItemDO> items = videoOrderItemMapper.listByOrderNo(orderNo);
        for (VideoOrderItemDO item : items) {
            VideoEntitlementDO existed = videoEntitlementMapper.findActiveByUserAndWork(userId, item.getWorkId());
            if (existed != null) {
                continue;
            }
            VideoEntitlementDO entitlement = new VideoEntitlementDO();
            entitlement.setUserId(userId);
            entitlement.setWorkId(item.getWorkId());
            entitlement.setOrderNo(orderNo);
            entitlement.setSource("PURCHASE");
            entitlement.setGrantedAt(LocalDateTime.now());
            entitlement.setExpireAt(null);
            entitlement.setStatus("ACTIVE");
            videoEntitlementMapper.insert(entitlement);
        }
    }

    private void requireInteractiveUser(Long userId) {
        UserDO userDO = userMapper.findById(userId);
        if (userDO == null || "GUEST".equalsIgnoreCase(userDO.getRole())) {
            throw new BizException(ErrorCodes.LOGIN_REQUIRED, "login required");
        }
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase();
    }

    private static String buildOrderNo() {
        long ts = System.currentTimeMillis();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "VM" + ts + suffix;
    }
}

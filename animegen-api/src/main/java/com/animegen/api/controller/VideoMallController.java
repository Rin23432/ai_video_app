package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.common.AuthContext;
import com.animegen.service.VideoMallService;
import com.animegen.service.dto.VideoMallCatalogListResponse;
import com.animegen.service.dto.VideoMallEntitlementListResponse;
import com.animegen.service.dto.VideoMallOrderCreateRequest;
import com.animegen.service.dto.VideoMallOrderCreateResponse;
import com.animegen.service.dto.VideoMallOrderListResponse;
import com.animegen.service.dto.VideoMallPaymentCallbackRequest;
import com.animegen.service.dto.VideoMallPaymentPrepayRequest;
import com.animegen.service.dto.VideoMallPaymentPrepayResponse;
import com.animegen.service.dto.VideoMallPlayAuthResponse;
import com.animegen.service.dto.VideoMallSkuDetailResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mall")
@Validated
public class VideoMallController {
    private final VideoMallService videoMallService;

    public VideoMallController(VideoMallService videoMallService) {
        this.videoMallService = videoMallService;
    }

    @GetMapping("/videos")
    public ApiResponse<VideoMallCatalogListResponse> listVideos(
            @RequestParam(name = "sort", defaultValue = "latest") String sort,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.ok(videoMallService.listVideos(sort, cursor, limit));
    }

    @GetMapping("/videos/{skuId}")
    public ApiResponse<VideoMallSkuDetailResponse> videoDetail(@PathVariable("skuId") @Min(1) Long skuId) {
        return ApiResponse.ok(videoMallService.getVideoDetail(skuId));
    }

    @PostMapping("/orders")
    public ApiResponse<VideoMallOrderCreateResponse> createOrder(@Valid @RequestBody VideoMallOrderCreateRequest request) {
        return ApiResponse.ok(videoMallService.createOrder(AuthContext.getUserId(), request));
    }

    @GetMapping("/me/orders")
    public ApiResponse<VideoMallOrderListResponse> myOrders(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.ok(videoMallService.myOrders(AuthContext.getUserId(), status, cursor, limit));
    }

    @GetMapping("/me/entitlements")
    public ApiResponse<VideoMallEntitlementListResponse> myEntitlements(
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit
    ) {
        return ApiResponse.ok(videoMallService.myEntitlements(AuthContext.getUserId(), cursor, limit));
    }

    @PostMapping("/payments/{orderNo}/prepay")
    public ApiResponse<VideoMallPaymentPrepayResponse> prepay(
            @PathVariable("orderNo") String orderNo,
            @Valid @RequestBody VideoMallPaymentPrepayRequest request
    ) {
        return ApiResponse.ok(videoMallService.prepay(AuthContext.getUserId(), orderNo, request));
    }

    @PostMapping("/payments/callback")
    public ApiResponse<Boolean> callback(@Valid @RequestBody VideoMallPaymentCallbackRequest request) {
        videoMallService.paymentCallback(request);
        return ApiResponse.ok(true);
    }

    @GetMapping("/play/auth/{workId}")
    public ApiResponse<VideoMallPlayAuthResponse> playAuth(@PathVariable("workId") @Min(1) Long workId) {
        return ApiResponse.ok(videoMallService.playAuth(AuthContext.getUserId(), workId));
    }
}

package com.animegen.dao.mapper;

import com.animegen.dao.domain.VideoMallSkuViewDO;
import com.animegen.dao.domain.VideoProductSkuDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VideoProductSkuMapper {
    @Select("SELECT s.id AS sku_id, s.spu_id, s.video_work_id AS work_id, p.title, p.subtitle, p.description, p.cover_url, " +
            "s.price_cent, s.origin_price_cent, s.currency, s.valid_days, s.stock_available, s.status, s.created_at " +
            "FROM video_product_sku s JOIN video_product_spu p ON p.id = s.spu_id " +
            "WHERE s.status = 'ON_SALE' AND p.status = 'ONLINE' " +
            "AND (#{cursor} = 0 OR s.id < #{cursor}) ORDER BY s.id DESC LIMIT #{limit}")
    List<VideoMallSkuViewDO> listOnSaleByLatest(@Param("cursor") Long cursor, @Param("limit") Integer limit);

    @Select("SELECT s.id AS sku_id, s.spu_id, s.video_work_id AS work_id, p.title, p.subtitle, p.description, p.cover_url, " +
            "s.price_cent, s.origin_price_cent, s.currency, s.valid_days, s.stock_available, s.status, s.created_at " +
            "FROM video_product_sku s JOIN video_product_spu p ON p.id = s.spu_id " +
            "WHERE s.status = 'ON_SALE' AND p.status = 'ONLINE' AND s.id = #{skuId}")
    VideoMallSkuViewDO findOnSaleDetail(@Param("skuId") Long skuId);

    @Select("SELECT * FROM video_product_sku WHERE id = #{skuId}")
    VideoProductSkuDO findById(@Param("skuId") Long skuId);

    @Update("UPDATE video_product_sku SET stock_available = stock_available - #{delta}, updated_at = NOW() " +
            "WHERE id = #{skuId} AND status = 'ON_SALE' AND stock_available >= #{delta}")
    int decreaseStockAvailable(@Param("skuId") Long skuId, @Param("delta") Integer delta);
}

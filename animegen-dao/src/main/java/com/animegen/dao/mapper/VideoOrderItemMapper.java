package com.animegen.dao.mapper;

import com.animegen.dao.domain.VideoOrderItemDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoOrderItemMapper {
    @Insert("INSERT INTO video_order_item(order_no, sku_id, spu_id, work_id, title_snapshot, cover_snapshot, unit_price_cent, quantity, amount_cent, created_at) " +
            "VALUES(#{orderNo}, #{skuId}, #{spuId}, #{workId}, #{titleSnapshot}, #{coverSnapshot}, #{unitPriceCent}, #{quantity}, #{amountCent}, NOW())")
    int insert(VideoOrderItemDO itemDO);

    @Select("SELECT * FROM video_order_item WHERE order_no = #{orderNo} ORDER BY id ASC")
    List<VideoOrderItemDO> listByOrderNo(@Param("orderNo") String orderNo);
}

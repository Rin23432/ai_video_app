package com.animegen.dao.mapper;

import com.animegen.dao.domain.VideoOrderDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VideoOrderMapper {
    @Insert("INSERT INTO video_order(order_no, user_id, total_amount_cent, pay_amount_cent, currency, status, pay_deadline_at, version, request_id, created_at, updated_at) " +
            "VALUES(#{orderNo}, #{userId}, #{totalAmountCent}, #{payAmountCent}, #{currency}, #{status}, #{payDeadlineAt}, #{version}, #{requestId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VideoOrderDO orderDO);

    @Select("SELECT * FROM video_order WHERE order_no = #{orderNo}")
    VideoOrderDO findByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM video_order WHERE user_id = #{userId} AND request_id = #{requestId} LIMIT 1")
    VideoOrderDO findByUserAndRequestId(@Param("userId") Long userId, @Param("requestId") String requestId);

    @Select("SELECT * FROM video_order WHERE user_id = #{userId} " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "AND (#{cursor} = 0 OR id < #{cursor}) " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<VideoOrderDO> listByUser(@Param("userId") Long userId,
                                  @Param("status") String status,
                                  @Param("cursor") Long cursor,
                                  @Param("limit") Integer limit);

    @Update("UPDATE video_order SET status='PAID', paid_at=NOW(), updated_at=NOW() " +
            "WHERE order_no=#{orderNo} AND status='PENDING_PAY'")
    int markPaid(@Param("orderNo") String orderNo);
}

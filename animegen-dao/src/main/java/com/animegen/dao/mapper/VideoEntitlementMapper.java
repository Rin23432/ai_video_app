package com.animegen.dao.mapper;

import com.animegen.dao.domain.VideoEntitlementDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VideoEntitlementMapper {
    @Insert("INSERT INTO video_entitlement(user_id, work_id, order_no, source, granted_at, expire_at, status, created_at, updated_at) " +
            "VALUES(#{userId}, #{workId}, #{orderNo}, #{source}, #{grantedAt}, #{expireAt}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VideoEntitlementDO entitlementDO);

    @Select("SELECT * FROM video_entitlement WHERE user_id = #{userId} " +
            "AND (#{cursor} = 0 OR id < #{cursor}) ORDER BY id DESC LIMIT #{limit}")
    List<VideoEntitlementDO> listByUser(@Param("userId") Long userId,
                                        @Param("cursor") Long cursor,
                                        @Param("limit") Integer limit);

    @Select("SELECT * FROM video_entitlement WHERE user_id = #{userId} AND work_id = #{workId} AND status = 'ACTIVE' LIMIT 1")
    VideoEntitlementDO findActiveByUserAndWork(@Param("userId") Long userId, @Param("workId") Long workId);
}

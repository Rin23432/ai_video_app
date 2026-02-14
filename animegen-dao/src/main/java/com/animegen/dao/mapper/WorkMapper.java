package com.animegen.dao.mapper;

import com.animegen.dao.domain.WorkDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WorkMapper {
    @Insert("INSERT INTO work(user_id, title, prompt, style_id, aspect_ratio, duration_sec, status, created_at, updated_at) " +
            "VALUES(#{userId}, #{title}, #{prompt}, #{styleId}, #{aspectRatio}, #{durationSec}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WorkDO workDO);

    @Select("SELECT * FROM work WHERE id = #{id}")
    WorkDO findById(@Param("id") Long id);

    @Select("SELECT * FROM work " +
            "WHERE user_id = #{userId} " +
            "AND (#{cursor} = 0 OR id < #{cursor}) " +
            "ORDER BY id DESC LIMIT #{limit}")
    List<WorkDO> listByUser(@Param("userId") Long userId, @Param("cursor") Long cursor, @Param("limit") Integer limit);

    @Update("UPDATE work SET status=#{status}, cover_url=#{coverUrl}, video_url=#{videoUrl}, updated_at=NOW() WHERE id=#{id}")
    int updateResult(WorkDO workDO);

    @Delete("DELETE FROM work WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
}

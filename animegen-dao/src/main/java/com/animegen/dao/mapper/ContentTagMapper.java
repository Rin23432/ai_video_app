package com.animegen.dao.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContentTagMapper {
    @Insert("INSERT INTO content_tag(content_id, tag_id, created_at) VALUES(#{contentId}, #{tagId}, NOW())")
    int insert(@Param("contentId") Long contentId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM content_tag WHERE content_id = #{contentId}")
    int deleteByContentId(@Param("contentId") Long contentId);

    @Select("SELECT tag_id FROM content_tag WHERE content_id = #{contentId} ORDER BY id ASC")
    List<Long> listTagIdsByContentId(@Param("contentId") Long contentId);
}

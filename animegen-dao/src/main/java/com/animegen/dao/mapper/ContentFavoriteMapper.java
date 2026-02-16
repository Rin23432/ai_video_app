package com.animegen.dao.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ContentFavoriteMapper {
    @Insert("INSERT INTO content_favorite(content_id, user_id, created_at) VALUES(#{contentId}, #{userId}, NOW())")
    int insert(@Param("contentId") Long contentId, @Param("userId") Long userId);

    @Delete("DELETE FROM content_favorite WHERE content_id = #{contentId} AND user_id = #{userId}")
    int delete(@Param("contentId") Long contentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) > 0 FROM content_favorite WHERE content_id = #{contentId} AND user_id = #{userId}")
    boolean exists(@Param("contentId") Long contentId, @Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM content_favorite WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}

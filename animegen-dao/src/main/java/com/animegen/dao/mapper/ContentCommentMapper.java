package com.animegen.dao.mapper;

import com.animegen.dao.domain.ContentCommentDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContentCommentMapper {
    @Insert("INSERT INTO content_comment(content_id, user_id, text, status, created_at) " +
            "VALUES(#{contentId}, #{userId}, #{text}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ContentCommentDO contentCommentDO);

    @Select("SELECT * FROM content_comment WHERE id = #{id}")
    ContentCommentDO findById(@Param("id") Long id);

    @Select("SELECT * FROM content_comment WHERE content_id = #{contentId} AND status = 'NORMAL' " +
            "ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentCommentDO> listByContent(@Param("contentId") Long contentId,
                                         @Param("offset") Long offset,
                                         @Param("limit") Integer limit);

    @Update("UPDATE content_comment SET status = 'DELETED' WHERE id = #{id} AND status = 'NORMAL'")
    int softDelete(@Param("id") Long id);
}

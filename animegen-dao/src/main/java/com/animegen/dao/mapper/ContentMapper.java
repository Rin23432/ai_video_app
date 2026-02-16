package com.animegen.dao.mapper;

import com.animegen.dao.domain.ContentDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContentMapper {
    @Insert("INSERT INTO content(work_id, author_id, title, description, media_type, cover_url, media_url, status, " +
            "like_count, favorite_count, comment_count, hot_score, publish_time, created_at, updated_at) " +
            "VALUES(#{workId}, #{authorId}, #{title}, #{description}, #{mediaType}, #{coverUrl}, #{mediaUrl}, #{status}, " +
            "0, 0, 0, 0, #{publishTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ContentDO contentDO);

    @Select("SELECT * FROM content WHERE id = #{id}")
    ContentDO findById(@Param("id") Long id);

    @Select("SELECT * FROM content WHERE id = #{id} AND status = 'PUBLISHED'")
    ContentDO findPublishedById(@Param("id") Long id);

    @Select("SELECT * FROM content WHERE work_id = #{workId} LIMIT 1")
    ContentDO findByWorkId(@Param("workId") Long workId);

    @Select("SELECT * FROM content WHERE author_id = #{authorId} AND id = #{id}")
    ContentDO findByAuthor(@Param("authorId") Long authorId, @Param("id") Long id);

    @Select("SELECT * FROM content WHERE status = 'PUBLISHED' " +
            "ORDER BY publish_time DESC, id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listPublishedByLatest(@Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT * FROM content WHERE status = 'PUBLISHED' " +
            "ORDER BY hot_score DESC, publish_time DESC, id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listPublishedByHot(@Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT c.* FROM content c " +
            "WHERE c.status = 'PUBLISHED' " +
            "AND (c.title LIKE CONCAT('%', #{keyword}, '%') OR c.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY " +
            "CASE " +
            "WHEN c.title = #{keyword} THEN 100 " +
            "WHEN c.title LIKE CONCAT(#{keyword}, '%') THEN 80 " +
            "WHEN c.title LIKE CONCAT('%', #{keyword}, '%') THEN 60 " +
            "WHEN c.description LIKE CONCAT('%', #{keyword}, '%') THEN 30 " +
            "ELSE 0 END DESC, " +
            "c.hot_score DESC, c.publish_time DESC, c.id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> searchPublished(@Param("keyword") String keyword,
                                    @Param("offset") Long offset,
                                    @Param("limit") Integer limit);

    @Select("SELECT c.* FROM content_tag ct JOIN content c ON c.id = ct.content_id " +
            "WHERE ct.tag_id = #{tagId} AND c.status = 'PUBLISHED' " +
            "ORDER BY c.publish_time DESC, c.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listPublishedByTagLatest(@Param("tagId") Long tagId, @Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT c.* FROM content_tag ct JOIN content c ON c.id = ct.content_id " +
            "WHERE ct.tag_id = #{tagId} AND c.status = 'PUBLISHED' " +
            "ORDER BY c.hot_score DESC, c.publish_time DESC, c.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listPublishedByTagHot(@Param("tagId") Long tagId, @Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT * FROM content WHERE author_id = #{authorId} AND status != 'REMOVED' " +
            "ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listMine(@Param("authorId") Long authorId, @Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT c.* FROM content_favorite f JOIN content c ON c.id = f.content_id " +
            "WHERE f.user_id = #{userId} AND c.status = 'PUBLISHED' " +
            "ORDER BY f.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ContentDO> listMyFavorites(@Param("userId") Long userId, @Param("offset") Long offset, @Param("limit") Integer limit);

    @Select("SELECT COUNT(1) FROM content WHERE author_id = #{authorId} AND status = 'PUBLISHED'")
    int countPublishedByAuthor(@Param("authorId") Long authorId);

    @Select("SELECT COALESCE(SUM(like_count), 0) FROM content WHERE author_id = #{authorId} AND status != 'REMOVED'")
    int sumLikesReceived(@Param("authorId") Long authorId);

    @Update("UPDATE content SET status = 'HIDDEN', updated_at = NOW() WHERE id = #{id} AND author_id = #{authorId} AND status = 'PUBLISHED'")
    int hideByAuthor(@Param("id") Long id, @Param("authorId") Long authorId);

    @Update("UPDATE content SET status = 'REMOVED', updated_at = NOW() WHERE id = #{id} AND author_id = #{authorId} AND status != 'REMOVED'")
    int removeByAuthor(@Param("id") Long id, @Param("authorId") Long authorId);

    @Update("UPDATE content SET like_count = GREATEST(like_count + #{likeDelta}, 0), " +
            "favorite_count = GREATEST(favorite_count + #{favoriteDelta}, 0), " +
            "comment_count = GREATEST(comment_count + #{commentDelta}, 0), " +
            "hot_score = GREATEST(hot_score + #{hotDelta}, 0), updated_at = NOW() WHERE id = #{contentId}")
    int updateCounters(@Param("contentId") Long contentId,
                       @Param("likeDelta") int likeDelta,
                       @Param("favoriteDelta") int favoriteDelta,
                       @Param("commentDelta") int commentDelta,
                       @Param("hotDelta") long hotDelta);
}

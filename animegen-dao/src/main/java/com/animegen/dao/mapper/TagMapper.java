package com.animegen.dao.mapper;

import com.animegen.dao.domain.TagDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {
    @Select("SELECT * FROM tag WHERE id = #{id} AND status = 'ACTIVE'")
    TagDO findActiveById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT * FROM tag WHERE status = 'ACTIVE' AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<TagDO> listActiveByIds(@Param("ids") List<Long> ids);

    @Select("SELECT * FROM tag WHERE status = 'ACTIVE' ORDER BY hot_score DESC, content_count DESC, id DESC LIMIT #{limit}")
    List<TagDO> listHot(@Param("limit") Integer limit);

    @Select("SELECT * FROM tag WHERE status = 'ACTIVE' AND name LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY hot_score DESC, content_count DESC, id DESC LIMIT #{limit}")
    List<TagDO> search(@Param("keyword") String keyword, @Param("limit") Integer limit);

    @Update("UPDATE tag SET content_count = GREATEST(content_count + #{delta}, 0), updated_at = NOW() WHERE id = #{tagId}")
    int updateContentCount(@Param("tagId") Long tagId, @Param("delta") int delta);

    @Update("UPDATE tag SET hot_score = GREATEST(hot_score + #{delta}, 0), updated_at = NOW() WHERE id = #{tagId}")
    int updateHotScore(@Param("tagId") Long tagId, @Param("delta") long delta);
}

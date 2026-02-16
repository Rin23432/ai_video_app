package com.animegen.dao.mapper;

import com.animegen.dao.domain.RankingSnapshotDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RankingSnapshotMapper {
    @Insert("INSERT INTO ranking_snapshot(rank_type, window, biz_date, rank_no, entity_id, score, meta_json, created_at) " +
            "VALUES(#{rankType}, #{window}, #{bizDate}, #{rankNo}, #{entityId}, #{score}, #{metaJson}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RankingSnapshotDO snapshotDO);

    @Delete("DELETE FROM ranking_snapshot WHERE rank_type = #{rankType} AND window = #{window} AND biz_date = #{bizDate}")
    int deleteByDay(@Param("rankType") String rankType, @Param("window") String window, @Param("bizDate") String bizDate);

    @Select("<script>" +
            "SELECT * FROM ranking_snapshot WHERE rank_type = #{rankType} AND window = #{window} AND biz_date = #{bizDate} " +
            "AND entity_id IN " +
            "<foreach collection='entityIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<RankingSnapshotDO> listByEntities(@Param("rankType") String rankType,
                                           @Param("window") String window,
                                           @Param("bizDate") String bizDate,
                                           @Param("entityIds") List<Long> entityIds);
}

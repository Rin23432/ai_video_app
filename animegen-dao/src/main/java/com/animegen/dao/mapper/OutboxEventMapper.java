package com.animegen.dao.mapper;

import com.animegen.dao.domain.OutboxEventDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OutboxEventMapper {
    @Insert("INSERT INTO outbox_event(event_type, aggregate_id, payload_json, status, retry_count, created_at, updated_at) " +
            "VALUES(#{eventType}, #{aggregateId}, #{payloadJson}, 'NEW', 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OutboxEventDO eventDO);

    @Select("SELECT * FROM outbox_event " +
            "WHERE (status = 'NEW' OR status = 'RETRY') " +
            "AND (next_retry_at IS NULL OR next_retry_at <= NOW()) " +
            "ORDER BY id ASC LIMIT #{limit}")
    List<OutboxEventDO> listPending(@Param("limit") int limit);

    @Update("UPDATE outbox_event SET status = 'PROCESSING', updated_at = NOW() " +
            "WHERE id = #{id} AND (status = 'NEW' OR status = 'RETRY')")
    int markProcessing(@Param("id") Long id);

    @Update("UPDATE outbox_event SET status = 'DONE', error_message = NULL, updated_at = NOW() WHERE id = #{id}")
    int markDone(@Param("id") Long id);

    @Update("UPDATE outbox_event SET status = 'RETRY', retry_count = retry_count + 1, " +
            "next_retry_at = DATE_ADD(NOW(), INTERVAL #{delaySec} SECOND), error_message = #{errorMessage}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int markRetry(@Param("id") Long id, @Param("delaySec") int delaySec, @Param("errorMessage") String errorMessage);

    @Update("UPDATE outbox_event SET status = 'FAILED', error_message = #{errorMessage}, updated_at = NOW() WHERE id = #{id}")
    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}

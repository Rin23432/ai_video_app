package com.animegen.dao.mapper;

import com.animegen.dao.domain.TaskDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TaskMapper {
    @Insert("INSERT INTO task(work_id, type, status, progress, stage, retry_count, trace_id, created_at, updated_at) " +
            "VALUES(#{workId}, #{type}, #{status}, #{progress}, #{stage}, #{retryCount}, #{traceId}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskDO taskDO);

    @Select("SELECT * FROM task WHERE id = #{id}")
    TaskDO findById(@Param("id") Long id);

    @Select("SELECT t.* FROM task t JOIN work w ON t.work_id = w.id WHERE t.id = #{taskId} AND w.user_id = #{userId}")
    TaskDO findByIdAndUser(@Param("taskId") Long taskId, @Param("userId") Long userId);

    @Update("UPDATE task " +
            "SET status=#{status}, progress=#{progress}, stage=#{stage}, error_code=#{errorCode}, error_message=#{errorMessage}, " +
            "retry_count=#{retryCount}, updated_at=NOW() " +
            "WHERE id=#{id}")
    int update(TaskDO taskDO);
}

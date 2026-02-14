package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.common.AuthContext;
import com.animegen.service.TaskService;
import com.animegen.service.dto.TaskStatusResponse;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@Validated
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskStatusResponse> detail(@PathVariable @Min(1) Long taskId) {
        return ApiResponse.ok(taskService.getTask(AuthContext.getUserId(), taskId));
    }
}

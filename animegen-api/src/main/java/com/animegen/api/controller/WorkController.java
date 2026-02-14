package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.common.AuthContext;
import com.animegen.dao.domain.WorkDO;
import com.animegen.service.WorkService;
import com.animegen.service.dto.CreateWorkRequest;
import com.animegen.service.dto.CreateWorkResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/works")
@Validated
public class WorkController {
    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @PostMapping
    public ApiResponse<CreateWorkResponse> create(@Valid @RequestBody CreateWorkRequest request) {
        return ApiResponse.ok(workService.createWork(AuthContext.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<List<WorkDO>> list(@RequestParam(defaultValue = "0") @Min(0) Long cursor,
                                          @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        return ApiResponse.ok(workService.listWorks(AuthContext.getUserId(), cursor, limit));
    }

    @GetMapping("/{workId}")
    public ApiResponse<WorkDO> detail(@PathVariable @Min(1) Long workId) {
        return ApiResponse.ok(workService.getWork(AuthContext.getUserId(), workId));
    }

    @DeleteMapping("/{workId}")
    public ApiResponse<Boolean> delete(@PathVariable @Min(1) Long workId) {
        workService.deleteWork(AuthContext.getUserId(), workId);
        return ApiResponse.ok(true);
    }
}

package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.common.AuthContext;
import com.animegen.service.CommunityService;
import com.animegen.service.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
@Validated
public class CommunityController {
    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/contents")
    public ApiResponse<CommunityPublishContentResponse> publish(@Valid @RequestBody CommunityPublishContentRequest request) {
        return ApiResponse.ok(communityService.publish(AuthContext.getUserId(), request));
    }

    @GetMapping("/contents")
    public ApiResponse<CommunityContentFeedResponse> feed(
            @RequestParam(defaultValue = "latest") String tab,
            @RequestParam(defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listContents(tab, cursor, limit));
    }

    @GetMapping("/contents/{contentId}")
    public ApiResponse<CommunityContentDetailResponse> detail(@PathVariable @Min(1) Long contentId) {
        return ApiResponse.ok(communityService.detail(AuthContext.getUserId(), contentId));
    }

    @PostMapping("/contents/{contentId}/like")
    public ApiResponse<CommunityToggleResponse> toggleLike(@PathVariable @Min(1) Long contentId,
                                                           @RequestBody(required = false) CommunityToggleActionRequest ignored) {
        return ApiResponse.ok(communityService.toggleLike(AuthContext.getUserId(), contentId));
    }

    @PostMapping("/contents/{contentId}/favorite")
    public ApiResponse<CommunityToggleResponse> toggleFavorite(@PathVariable @Min(1) Long contentId,
                                                               @RequestBody(required = false) CommunityToggleActionRequest ignored) {
        return ApiResponse.ok(communityService.toggleFavorite(AuthContext.getUserId(), contentId));
    }

    @GetMapping("/contents/{contentId}/comments")
    public ApiResponse<CommunityCommentListResponse> listComments(@PathVariable @Min(1) Long contentId,
                                                                  @RequestParam(defaultValue = "0") @Min(0) Long cursor,
                                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listComments(contentId, cursor, limit));
    }

    @PostMapping("/contents/{contentId}/comments")
    public ApiResponse<CommunityCreateCommentResponse> createComment(@PathVariable @Min(1) Long contentId,
                                                                     @Valid @RequestBody CommunityCreateCommentRequest request) {
        return ApiResponse.ok(communityService.createComment(AuthContext.getUserId(), contentId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Boolean> deleteComment(@PathVariable @Min(1) Long commentId) {
        communityService.deleteComment(AuthContext.getUserId(), commentId);
        return ApiResponse.ok(true);
    }

    @GetMapping("/me/favorites")
    public ApiResponse<CommunityContentFeedResponse> myFavorites(@RequestParam(defaultValue = "0") @Min(0) Long cursor,
                                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.myFavorites(AuthContext.getUserId(), cursor, limit));
    }

    @GetMapping("/me/contents")
    public ApiResponse<CommunityContentFeedResponse> myContents(@RequestParam(defaultValue = "0") @Min(0) Long cursor,
                                                                @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.myPublished(AuthContext.getUserId(), cursor, limit));
    }

    @PostMapping("/contents/{contentId}/hide")
    public ApiResponse<Boolean> hide(@PathVariable @Min(1) Long contentId) {
        communityService.hide(AuthContext.getUserId(), contentId);
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/contents/{contentId}")
    public ApiResponse<Boolean> remove(@PathVariable @Min(1) Long contentId) {
        communityService.remove(AuthContext.getUserId(), contentId);
        return ApiResponse.ok(true);
    }
}

package com.animegen.api.controller;

import com.animegen.common.ApiResponse;
import com.animegen.common.AuthContext;
import com.animegen.service.CommunityService;
import com.animegen.service.dto.*;
import com.animegen.service.ranking.RankingService;
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
    private final RankingService rankingService;

    public CommunityController(CommunityService communityService, RankingService rankingService) {
        this.communityService = communityService;
        this.rankingService = rankingService;
    }

    @PostMapping("/contents")
    public ApiResponse<CommunityPublishContentResponse> publish(@Valid @RequestBody CommunityPublishContentRequest request) {
        return ApiResponse.ok(communityService.publish(AuthContext.getUserId(), request));
    }

    @GetMapping("/contents")
    public ApiResponse<CommunityContentFeedResponse> feed(
            @RequestParam(name = "tab", defaultValue = "latest") String tab,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listContents(tab, cursor, limit));
    }

    @GetMapping("/contents/search")
    public ApiResponse<CommunityContentFeedResponse> searchContents(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.searchContents(keyword, cursor, limit));
    }

    @GetMapping("/contents/{contentId}")
    public ApiResponse<CommunityContentDetailResponse> detail(@PathVariable("contentId") @Min(1) Long contentId) {
        return ApiResponse.ok(communityService.detail(AuthContext.getUserId(), contentId));
    }

    @GetMapping("/tags/hot")
    public ApiResponse<CommunityTagListResponse> hotTags(
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listHotTags(limit));
    }

    @GetMapping("/tags/search")
    public ApiResponse<CommunityTagListResponse> searchTags(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.searchTags(keyword, limit));
    }

    @GetMapping("/tags/{tagId}")
    public ApiResponse<CommunityTagDetailResponse> tagDetail(@PathVariable("tagId") @Min(1) Long tagId) {
        return ApiResponse.ok(communityService.tagDetail(tagId));
    }

    @GetMapping("/tags/{tagId}/contents")
    public ApiResponse<CommunityContentFeedResponse> tagContents(
            @PathVariable("tagId") @Min(1) Long tagId,
            @RequestParam(name = "tab", defaultValue = "latest") String tab,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listTagContents(tagId, tab, cursor, limit));
    }

    @PostMapping("/contents/{contentId}/like")
    public ApiResponse<CommunityToggleResponse> toggleLike(@PathVariable("contentId") @Min(1) Long contentId,
                                                           @RequestBody(required = false) CommunityToggleActionRequest ignored) {
        return ApiResponse.ok(communityService.toggleLike(AuthContext.getUserId(), contentId));
    }

    @PostMapping("/contents/{contentId}/favorite")
    public ApiResponse<CommunityToggleResponse> toggleFavorite(@PathVariable("contentId") @Min(1) Long contentId,
                                                               @RequestBody(required = false) CommunityToggleActionRequest ignored) {
        return ApiResponse.ok(communityService.toggleFavorite(AuthContext.getUserId(), contentId));
    }

    @GetMapping("/contents/{contentId}/comments")
    public ApiResponse<CommunityCommentListResponse> listComments(@PathVariable("contentId") @Min(1) Long contentId,
                                                                  @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
                                                                  @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.listComments(contentId, cursor, limit));
    }

    @PostMapping("/contents/{contentId}/comments")
    public ApiResponse<CommunityCreateCommentResponse> createComment(@PathVariable("contentId") @Min(1) Long contentId,
                                                                     @Valid @RequestBody CommunityCreateCommentRequest request) {
        return ApiResponse.ok(communityService.createComment(AuthContext.getUserId(), contentId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Boolean> deleteComment(@PathVariable("commentId") @Min(1) Long commentId) {
        communityService.deleteComment(AuthContext.getUserId(), commentId);
        return ApiResponse.ok(true);
    }

    @GetMapping("/me/favorites")
    public ApiResponse<CommunityContentFeedResponse> myFavorites(@RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
                                                                 @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.myFavorites(AuthContext.getUserId(), cursor, limit));
    }

    @GetMapping("/me/contents")
    public ApiResponse<CommunityContentFeedResponse> myContents(@RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
                                                                @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(communityService.myPublished(AuthContext.getUserId(), cursor, limit));
    }

    @PostMapping("/contents/{contentId}/hide")
    public ApiResponse<Boolean> hide(@PathVariable("contentId") @Min(1) Long contentId) {
        communityService.hide(AuthContext.getUserId(), contentId);
        return ApiResponse.ok(true);
    }

    @DeleteMapping("/contents/{contentId}")
    public ApiResponse<Boolean> remove(@PathVariable("contentId") @Min(1) Long contentId) {
        communityService.remove(AuthContext.getUserId(), contentId);
        return ApiResponse.ok(true);
    }

    @GetMapping("/rankings/contents")
    public ApiResponse<CommunityRankingContentResponse> contentRankings(
            @RequestParam(name = "window", defaultValue = "weekly") String window,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(rankingService.listContentRankings(window, cursor, limit));
    }

    @GetMapping("/rankings/authors")
    public ApiResponse<CommunityRankingAuthorResponse> authorRankings(
            @RequestParam(name = "window", defaultValue = "weekly") String window,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(rankingService.listAuthorRankings(window, cursor, limit));
    }

    @GetMapping("/rankings/tags")
    public ApiResponse<CommunityRankingTagResponse> tagRankings(
            @RequestParam(name = "window", defaultValue = "weekly") String window,
            @RequestParam(name = "cursor", defaultValue = "0") @Min(0) Long cursor,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        return ApiResponse.ok(rankingService.listTagRankings(window, cursor, limit));
    }
}

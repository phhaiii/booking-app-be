
package com.myapp.booking.controllers;

import com.myapp.booking.configurations.SecurityUtils;
import com.myapp.booking.dtos.requests.CreateCommentRequest;
import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.responses.CommentResponse;
import com.myapp.booking.dtos.responses.CommentsPageResponse;
import com.myapp.booking.services.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;


@Slf4j
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "APIs for managing post comments and ratings")
public class CommentController {

    private final CommentService commentService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Create comment", description = "Add comment and rating to post with optional images")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @RequestParam("rating") BigDecimal rating,
            Authentication authentication) {

        log.info("💬 POST /api/posts/{}/comments - Create comment", postId);
        log.info("Content: {}", content);
        log.info("Rating: {}", rating);

        // Build request object
        CreateCommentRequest request = new CreateCommentRequest();
        request.setContent(content);
        request.setRating(rating);

        Long userId = SecurityUtils.getUserId(authentication);
        CommentResponse response = commentService.createComment(request, postId, userId);

        log.info("✅ Comment created successfully with ID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Comment created successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // GET COMMENTS BY POST
    // ═══════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(summary = "Get comments by post", description = "Get all comments for a post with pagination")
    public ResponseEntity<ApiResponse<CommentsPageResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        log.info("📋 GET /api/posts/{}/comments - Get comments", postId);
        log.info("Page: {}, Size: {}, Sort: {}", page, size, sortBy);

        CommentsPageResponse response = commentService.getCommentsByPost(postId, page, size, sortBy);

        log.info("✅ Found {} comments", response.getTotalElements());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Comments retrieved successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // GET COMMENT BY ID
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/{commentId}")
    @Operation(summary = "Get comment by ID", description = "Get comment details")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        log.info("🔍 GET /api/posts/{}/comments/{} - Get comment", postId, commentId);

        CommentResponse response = commentService.getCommentById(commentId);

        log.info("✅ Found comment");

        return ResponseEntity.ok(
                ApiResponse.success(response, "Comment retrieved successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // GET COMMENT STATISTICS
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/statistics")
    @Operation(summary = "Get comment statistics", description = "Get rating statistics for a post")
    public ResponseEntity<ApiResponse<CommentsPageResponse.RatingDistribution>> getStatistics(
            @PathVariable Long postId) {

        log.info("📊 GET /api/posts/{}/comments/statistics", postId);

        CommentsPageResponse response = commentService.getCommentsByPost(postId, 0, 1, "createdAt");

        log.info("✅ Statistics retrieved");

        return ResponseEntity.ok(
                ApiResponse.success(response.getRatingDistribution(), "Statistics retrieved successfully"));
    }
}

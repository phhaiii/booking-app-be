package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.post.CreatePostRequest;
import com.myapp.booking.dtos.requests.post.UpdatePostRequest;
import com.myapp.booking.models.Post;
import com.myapp.booking.dtos.responses.post.PostResponse;
import com.myapp.booking.dtos.responses.post.PostStatisticsResponse;
import com.myapp.booking.dtos.responses.VendorStatisticsResponse;
import com.myapp.booking.dtos.responses.post.PostListResponse;
import com.myapp.booking.services.interfaces.IPostService;
import com.myapp.booking.dtos.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "APIs for managing posts")
public class PostController {

    private final IPostService postService;

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Create new post", description = "Vendor creates a new post")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        PostResponse response = postService.createPost(request, vendorId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response,"Post created successfully"));
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Update post", description = "Vendor updates their post")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        PostResponse response = postService.updatePost(postId, request, vendorId);

        return ResponseEntity.ok(ApiResponse.success(response,"Post updated successfully"));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Delete post", description = "Vendor deletes their post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        postService.deletePost(postId, vendorId);

        return ResponseEntity.ok(ApiResponse.success(null,"Post deleted successfully" ));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get post by ID", description = "Get detailed information about a post")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        PostResponse response = postService.getPostById(postId);

        // Increment view count asynchronously
        postService.incrementViewCount(postId);

        return ResponseEntity.ok(ApiResponse.success(response,"Post retrieved successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all published posts", description = "Get list of all published posts")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getAllPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostListResponse> posts = postService.getPublishedPosts(pageable);
        return ResponseEntity.ok(ApiResponse.success(posts,"Posts retrieved successfully"));
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get my posts", description = "Vendor gets their own posts")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostListResponse> posts = postService.getMyPosts(vendorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(posts,"My posts retrieved successfully"));
    }

    @GetMapping("/vendor/{vendorId}")
    @Operation(summary = "Get posts by vendor", description = "Get all posts from a specific vendor")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPostsByVendor(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostListResponse> posts = postService.getPostsByVendor(vendorId, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Vendor posts retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts", description = "Search posts by keyword")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostListResponse> posts = postService.searchPosts(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Search results retrieved successfully"));
    }

    @GetMapping("/filter/price")
    @Operation(summary = "Filter by price range", description = "Filter posts by price range")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> filterByPrice(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<PostListResponse> posts = postService.filterByPriceRange(minPrice, maxPrice, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Filtered posts retrieved successfully"));
    }

    @GetMapping("/filter/capacity")
    @Operation(summary = "Filter by capacity", description = "Filter posts by minimum capacity")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> filterByCapacity(
            @RequestParam Integer minCapacity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("capacity").ascending());
        Page<PostListResponse> posts = postService.filterByCapacity(minCapacity, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Filtered posts retrieved successfully"));
    }

    @GetMapping("/filter/style")
    @Operation(summary = "Filter by style", description = "Filter posts by style")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> filterByStyle(
            @RequestParam String style,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostListResponse> posts = postService.filterByStyle(style, pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Filtered posts retrieved successfully"));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular posts", description = "Get posts sorted by view count")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostListResponse> posts = postService.getPopularPosts(pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Popular posts retrieved successfully"));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending posts", description = "Get posts sorted by booking count")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getTrendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostListResponse> posts = postService.getTrendingPosts(pageable);

        return ResponseEntity.ok(ApiResponse.success(posts,"Trending posts retrieved successfully"));
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR')")
    @Operation(summary = "Toggle like", description = "Like or unlike a post")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);
        postService.toggleLike(postId, userId);

        return ResponseEntity.ok(ApiResponse.success(null,"Like toggled successfully"));
    }

    @PatchMapping("/{postId}/status")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Change post status", description = "Vendor changes post status")
    public ResponseEntity<ApiResponse<PostResponse>> changePostStatus(
            @PathVariable Long postId,
            @RequestParam Post.PostStatus status,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        PostResponse response = postService.changePostStatus(postId, status, vendorId);

        return ResponseEntity.ok(ApiResponse.success(response,"Post status changed successfully"));
    }

    @GetMapping("/{postId}/statistics")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get post statistics", description = "Vendor gets statistics for their post")
    public ResponseEntity<ApiResponse<PostStatisticsResponse>> getPostStatistics(
            @PathVariable Long postId,
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        PostStatisticsResponse stats = postService.getPostStatistics(postId, vendorId);

        return ResponseEntity.ok(ApiResponse.success(stats,"Statistics retrieved successfully"));
    }

    @GetMapping("/statistics/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get vendor statistics", description = "Vendor gets their overall statistics")
    public ResponseEntity<ApiResponse<VendorStatisticsResponse>> getVendorStatistics(
            Authentication authentication) {

        Long vendorId = extractUserId(authentication);
        VendorStatisticsResponse stats = postService.getVendorStatistics(vendorId);

        return ResponseEntity.ok(ApiResponse.success(stats,"Vendor statistics retrieved successfully"));
    }

    // Helper method to extract user ID from authentication
    private Long extractUserId(Authentication authentication) {
        // Assuming you have a custom UserDetails implementation
        // Adjust this based on your actual authentication setup
        return ((com.myapp.booking.security.CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
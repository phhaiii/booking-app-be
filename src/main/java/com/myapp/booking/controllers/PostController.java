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
import com.myapp.booking.configurations.SecurityUtils;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "APIs for managing posts")
public class PostController {

    private final IPostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Create new post", description = "Vendor creates a new post")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("content") String content,
            @RequestParam("location") String location,
            @RequestParam("price") BigDecimal price,
            @RequestParam("capacity") Integer capacity,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "allowComments", defaultValue = "true") Boolean allowComments,
            @RequestParam(value = "enableNotifications", defaultValue = "true") Boolean enableNotifications,
            @RequestParam(value = "amenities", required = false) String amenitiesJson,
            @RequestParam(value = "menuItems", required = false) String menuItemsJson,
            @RequestPart(value = "images") List<MultipartFile> images,
            Authentication authentication) {

        Long userId = SecurityUtils.getUserId(authentication);
        String userRole = SecurityUtils.getUserRole(authentication);

        log.info("═══════════════════════════════════════");
        log.info("📥 RECEIVED CREATE POST REQUEST");
        log.info("Title: {}", title);
        log.info("Location: {}", location);
        log.info("Images count: {}", images.size());
        log.info("Menu items JSON: {}", menuItemsJson);

        // ✅ Parse amenities JSON
        Set<String> amenities = new HashSet<>();
        if (amenitiesJson != null && !amenitiesJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                amenities = mapper.readValue(amenitiesJson, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Error parsing amenities JSON: {}", e.getMessage());
            }
        }

        List<Map<String, Object>> menuItemsList = new ArrayList<>();
        if (menuItemsJson != null && !menuItemsJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                menuItemsList = mapper.readValue(menuItemsJson, new TypeReference<>() {});
                log.info("✅ Parsed {} menu items", menuItemsList.size());
            } catch (Exception e) {
                log.error("Error parsing menuItems JSON: {}", e.getMessage());
            }
        }

        // ✅ Build request DTO
        CreatePostRequest request = CreatePostRequest.builder()
                .title(title)
                .description(description)
                .content(content)
                .location(location)
                .price(price)
                .capacity(capacity)
                .style(style)
                .images(images)
                .amenities(amenities)
                .allowComments(allowComments)
                .enableNotifications(enableNotifications)
                .build();

        PostResponse response = postService.createPost(request, userId);

        // ✅ Create menus if provided
        if (!menuItemsList.isEmpty()) {
            log.info("🍽️ Creating {} menus for post {}", menuItemsList.size(), response.getId());
            // TODO: Create menus using MenuService
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Post created successfully by " + userRole));
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Update post", description = "Vendor updates their post with optional new images")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "allowComments", required = false) Boolean allowComments,
            @RequestParam(value = "enableNotifications", required = false) Boolean enableNotifications,
            @RequestParam(value = "amenities", required = false) String amenitiesJson,
            @RequestParam(value = "existingImages", required = false) String existingImagesJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);

        log.info("═══════════════════════════════════════");
        log.info("📝 UPDATING POST: {}", postId);
        log.info("Vendor ID: {}", vendorId);
        log.info("New images count: {}", newImages != null ? newImages.size() : 0);

        // Parse amenities JSON
        Set<String> amenities = new HashSet<>();
        if (amenitiesJson != null && !amenitiesJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                amenities = mapper.readValue(amenitiesJson, new TypeReference<>() {});
            } catch (Exception e) {
                log.error("Error parsing amenities JSON: {}", e.getMessage());
            }
        }

        // Parse existing images JSON
        List<String> existingImages = new ArrayList<>();
        if (existingImagesJson != null && !existingImagesJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                existingImages = mapper.readValue(existingImagesJson, new TypeReference<>() {});
                log.info("✅ Keeping {} existing images", existingImages.size());
            } catch (Exception e) {
                log.error("Error parsing existing images JSON: {}", e.getMessage());
            }
        }

        // Build update request
        UpdatePostRequest request = UpdatePostRequest.builder()
                .title(title)
                .description(description)
                .content(content)
                .location(location)
                .price(price)
                .capacity(capacity)
                .style(style)
                .amenities(amenities)
                .allowComments(allowComments)
                .enableNotifications(enableNotifications)
                .existingImages(existingImages)
                .build();

        PostResponse response = postService.updatePost(postId, request, newImages, vendorId);

        log.info("✅ Post updated successfully");
        log.info("═══════════════════════════════════════");

        return ResponseEntity.ok(ApiResponse.success(response, "Post updated successfully"));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Delete post", description = "Vendor deletes their post")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);
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
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Get my posts", description = "Vendor gets their own posts")
    public ResponseEntity<ApiResponse<Page<PostListResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);
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
    @PreAuthorize("hasAnyRole('USER', 'VENDOR','ADMIN')")
    @Operation(summary = "Toggle like", description = "Like or unlike a post")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = SecurityUtils.getUserId(authentication);
        postService.toggleLike(postId, userId);

        return ResponseEntity.ok(ApiResponse.success(null,"Like toggled successfully"));
    }

    @PatchMapping("/{postId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Change post status", description = "Vendor changes post status")
    public ResponseEntity<ApiResponse<PostResponse>> changePostStatus(
            @PathVariable Long postId,
            @RequestParam Post.PostStatus status,
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);
        PostResponse response = postService.changePostStatus(postId, status, vendorId);

        return ResponseEntity.ok(ApiResponse.success(response,"Post status changed successfully"));
    }

    @GetMapping("/{postId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Get post statistics", description = "Vendor gets statistics for their post")
    public ResponseEntity<ApiResponse<PostStatisticsResponse>> getPostStatistics(
            @PathVariable Long postId,
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);
        PostStatisticsResponse stats = postService.getPostStatistics(postId, vendorId);

        return ResponseEntity.ok(ApiResponse.success(stats,"Statistics retrieved successfully"));
    }

    @GetMapping("/statistics/vendor")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Get vendor statistics", description = "Vendor gets their overall statistics")
    public ResponseEntity<ApiResponse<VendorStatisticsResponse>> getVendorStatistics(
            Authentication authentication) {

        Long vendorId = SecurityUtils.getUserId(authentication);
        VendorStatisticsResponse stats = postService.getVendorStatistics(vendorId);

        return ResponseEntity.ok(ApiResponse.success(stats,"Vendor statistics retrieved successfully"));
    }
}
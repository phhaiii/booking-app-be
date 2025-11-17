package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.post.CreatePostRequest;
import com.myapp.booking.dtos.requests.post.UpdatePostRequest;
import com.myapp.booking.models.Post;
import com.myapp.booking.dtos.responses.post.PostStatisticsResponse;
import com.myapp.booking.dtos.responses.VendorStatisticsResponse;
import com.myapp.booking.dtos.responses.post.PostResponse;
import com.myapp.booking.dtos.responses.post.PostListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface IPostService {

    // Create new post
    PostResponse createPost(CreatePostRequest request, Long vendorId);

    // Update post with optional new images
    PostResponse updatePost(Long postId, UpdatePostRequest request, List<MultipartFile> newImages, Long vendorId);

    // Delete post (soft delete)
    void deletePost(Long postId, Long vendorId);

    // Get post by id
    PostResponse getPostById(Long postId);

    // Get all posts (for admin)
    Page<PostListResponse> getAllPosts(Pageable pageable);

    // Get published posts (for customers)
    Page<PostListResponse> getPublishedPosts(Pageable pageable);

    // Get posts by vendor
    Page<PostListResponse> getPostsByVendor(Long vendorId, Pageable pageable);

    // Get my posts (vendor)
    Page<PostListResponse> getMyPosts(Long vendorId, Pageable pageable);

    // Get posts by status
    Page<PostListResponse> getPostsByStatus(Post.PostStatus status, Pageable pageable);

    // Search posts
    Page<PostListResponse> searchPosts(String keyword, Pageable pageable);

    // Filter posts by price range
    Page<PostListResponse> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Filter posts by capacity
    Page<PostListResponse> filterByCapacity(Integer minCapacity, Pageable pageable);

    // Filter posts by style
    Page<PostListResponse> filterByStyle(String style, Pageable pageable);

    // Get popular posts
    Page<PostListResponse> getPopularPosts(Pageable pageable);

    // Get trending posts
    Page<PostListResponse> getTrendingPosts(Pageable pageable);

    // Increment view count
    void incrementViewCount(Long postId);

    // Toggle like
    void toggleLike(Long postId, Long userId);

    // Change post status
    PostResponse changePostStatus(Long postId, Post.PostStatus status, Long vendorId);

    // Get post statistics
    PostStatisticsResponse getPostStatistics(Long postId, Long vendorId);

    // Get vendor statistics
    VendorStatisticsResponse getVendorStatistics(Long vendorId);
}
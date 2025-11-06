package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.post.CreatePostRequest;
import com.myapp.booking.dtos.requests.post.UpdatePostRequest;
import com.myapp.booking.models.Post;
import com.myapp.booking.models.User;
import com.myapp.booking.dtos.responses.post.PostResponse;
import com.myapp.booking.dtos.responses.post.PostListResponse;
import com.myapp.booking.dtos.responses.post.PostStatisticsResponse;
import com.myapp.booking.dtos.responses.VendorStatisticsResponse;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.exceptions.UnauthorizedException;
import com.myapp.booking.repositories.PostRepository;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.services.interfaces.IPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService implements IPostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostResponse createPost(CreatePostRequest request, Long vendorId) {
        log.info("Creating post for vendor: {}", vendorId);

        // Validate vendor
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        if (!"VENDOR".equals(vendor.getRole())) {
            throw new UnauthorizedException("Only vendors can create posts");
        }

        // Create post entity
        Post post = Post.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .location(request.getLocation())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .style(request.getStyle())
                .images(request.getImages())
                .amenities(request.getAmenities() != null ? request.getAmenities() : new HashSet<>())
                .allowComments(request.getAllowComments())
                .enableNotifications(request.getEnableNotifications())
                .status(Post.PostStatus.PUBLISHED)
                .vendor(vendor)
                .isActive(true)
                .isDeleted(false)
                .build();

        Post savedPost = postRepository.save(post);
        log.info("Post created successfully with ID: {}", savedPost.getId());

        return PostResponse.fromEntity(savedPost);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, UpdatePostRequest request, Long vendorId) {
        log.info("Updating post: {} by vendor: {}", postId, vendorId);

        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check ownership
        if (!post.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't have permission to update this post");
        }

        // Update fields if provided
        if (request.getTitle() != null) post.setTitle(request.getTitle());
        if (request.getDescription() != null) post.setDescription(request.getDescription());
        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getLocation() != null) post.setLocation(request.getLocation());
        if (request.getPrice() != null) post.setPrice(request.getPrice());
        if (request.getCapacity() != null) post.setCapacity(request.getCapacity());
        if (request.getStyle() != null) post.setStyle(request.getStyle());
        if (request.getImages() != null) post.setImages(request.getImages());
        if (request.getAmenities() != null) post.setAmenities(request.getAmenities());
        if (request.getAllowComments() != null) post.setAllowComments(request.getAllowComments());
        if (request.getEnableNotifications() != null) post.setEnableNotifications(request.getEnableNotifications());

        Post updatedPost = postRepository.save(post);
        log.info("Post updated successfully: {}", postId);

        return PostResponse.fromEntity(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long vendorId) {
        log.info("Deleting post: {} by vendor: {}", postId, vendorId);

        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check ownership
        if (!post.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't have permission to delete this post");
        }

        post.setIsDeleted(true);
        post.setIsActive(false);
        postRepository.save(post);

        log.info("Post deleted successfully: {}", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        log.info("Getting post by ID: {}", postId);

        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return PostResponse.fromEntity(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getAllPosts(Pageable pageable) {
        log.info("Getting all posts");
        return postRepository.findAll(pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getPublishedPosts(Pageable pageable) {
        log.info("Getting published posts");
        return postRepository.findAllPublishedPosts(pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostsByVendor(Long vendorId, Pageable pageable) {
        log.info("Getting posts by vendor: {}", vendorId);

        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        return postRepository.findByVendorAndIsDeletedFalse(vendor, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getMyPosts(Long vendorId, Pageable pageable) {
        return getPostsByVendor(vendorId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostsByStatus(Post.PostStatus status, Pageable pageable) {
        log.info("Getting posts by status: {}", status);
        return postRepository.findByStatusAndIsDeletedFalse(status, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> searchPosts(String keyword, Pageable pageable) {
        log.info("Searching posts with keyword: {}", keyword);
        return postRepository.searchPosts(keyword, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.info("Filtering posts by price range: {} - {}", minPrice, maxPrice);
        return postRepository.findByPriceRange(minPrice, maxPrice, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> filterByCapacity(Integer minCapacity, Pageable pageable) {
        log.info("Filtering posts by capacity: {}", minCapacity);
        return postRepository.findByMinCapacity(minCapacity, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> filterByStyle(String style, Pageable pageable) {
        log.info("Filtering posts by style: {}", style);
        return postRepository.findByStyleAndStatusAndIsDeletedFalse(style, Post.PostStatus.PUBLISHED, pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getPopularPosts(Pageable pageable) {
        log.info("Getting popular posts");
        return postRepository.findPopularPosts(pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostListResponse> getTrendingPosts(Pageable pageable) {
        log.info("Getting trending posts");
        return postRepository.findTrendingPosts(pageable)
                .map(PostListResponse::fromEntity);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long postId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.incrementViewCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void toggleLike(Long postId, Long userId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // TODO: Implement like/unlike logic with a separate PostLike entity
        // For now, just increment/decrement like count
        post.incrementLikeCount();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public PostResponse changePostStatus(Long postId, Post.PostStatus status, Long vendorId) {
        log.info("Changing post status: {} to {}", postId, status);

        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't have permission to change this post status");
        }

        post.setStatus(status);
        Post updatedPost = postRepository.save(post);

        return PostResponse.fromEntity(updatedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public PostStatisticsResponse getPostStatistics(Long postId, Long vendorId) {
        Post post = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getVendor().getId().equals(vendorId)) {
            throw new UnauthorizedException("You don't have permission to view this post statistics");
        }

        return PostStatisticsResponse.builder()
                .postId(postId)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .bookingCount(post.getBookingCount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorStatisticsResponse getVendorStatistics(Long vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        Long totalPosts = postRepository.countByVendorAndIsDeletedFalse(vendor);
        Long publishedPosts = postRepository.countByVendorAndStatusAndIsDeletedFalse(
                vendor, Post.PostStatus.PUBLISHED);
        Long draftPosts = postRepository.countByVendorAndStatusAndIsDeletedFalse(
                vendor, Post.PostStatus.DRAFT);

        return VendorStatisticsResponse.builder()
                .vendorId(vendorId)
                .totalPosts(totalPosts)
                .publishedPosts(publishedPosts)
                .draftPosts(draftPosts)
                .build();
    }
}
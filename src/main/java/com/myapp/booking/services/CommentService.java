
package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.CreateCommentRequest;
import com.myapp.booking.dtos.responses.CommentResponse;
import com.myapp.booking.dtos.responses.CommentsPageResponse;
import com.myapp.booking.exceptions.BadRequestException;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.models.Comment;
import com.myapp.booking.models.Post;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.CommentRepository;
import com.myapp.booking.repositories.PostRepository;
import com.myapp.booking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ═══════════════════════════════════════════════════════════════
    // CREATE COMMENT - VỚI UPLOAD ẢNH
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public CommentResponse createComment(
            CreateCommentRequest request,
            Long postId,
            Long userId
    ) {
        log.info("💬 CREATING COMMENT");
        log.info("Post ID: {}", postId);
        log.info("User ID: {}", userId);
        log.info("Rating: {}", request.getRating());


        // Validate
        validateCreateCommentRequest(request);

        // Check post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ✅ Check if user already commented (only 1 comment per user per post)
        if (commentRepository.existsByPostIdAndUserIdAndIsActiveTrue(postId, userId)) {
            throw new BadRequestException("You have already commented on this post");
        }


        // TODO: Check if user has booking for this post
        Boolean isVerifiedBooking = false; // Implement booking check logic

        // Create comment
        Comment comment = Comment.builder()
                .content(request.getContent())
                .rating(request.getRating())
                .isVerifiedBooking(isVerifiedBooking)
                .post(post)
                .user(user)
                .isActive(true)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Update post comment count
        post.incrementCommentCount();
        postRepository.save(post);

        log.info("✅ Comment created successfully with ID: {}", savedComment.getId());
        log.info("═══════════════════════════════════════");

        return mapToResponse(savedComment);
    }
    // ═══════════════════════════════════════════════════════════════
    // GET COMMENTS
    // ═══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public CommentsPageResponse getCommentsByPost(
            Long postId,
            int page,
            int size,
            String sortBy
    ) {
        log.info("📋 Getting comments for post: {}", postId);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        // Create pageable
        Sort sort = sortBy.equals("rating")
                ? Sort.by(Sort.Direction.DESC, "rating", "createdAt")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(page, size, sort);

        // Get comments
        Page<Comment> commentsPage = commentRepository.findByPostIdAndIsActiveTrue(postId, pageable);

        // Get statistics
        BigDecimal averageRating = commentRepository.calculateAverageRating(postId);
        Long totalComments = commentRepository.countByPostIdAndIsActiveTrue(postId);
        CommentsPageResponse.RatingDistribution distribution = getRatingDistribution(postId);

        log.info("✅ Found {} comments for post {}", commentsPage.getTotalElements(), postId);

        return CommentsPageResponse.builder()
                .comments(commentsPage.getContent().stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList()))
                .currentPage(page)
                .totalPages(commentsPage.getTotalPages())
                .totalElements(commentsPage.getTotalElements())
                .hasNext(commentsPage.hasNext())
                .averageRating(averageRating != null ? averageRating : BigDecimal.ZERO)
                .totalComments(totalComments)
                .ratingDistribution(distribution)
                .build();
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        log.info("🔍 Getting comment by ID: {}", commentId);

        Comment comment = commentRepository.findByIdAndIsActiveTrue(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comment not found or inactive with id: " + commentId));

        log.info("✅ Found comment: {}", comment.getId());

        return mapToResponse(comment);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    private void validateCreateCommentRequest(CreateCommentRequest request) {
        log.debug("Validating create comment request...");

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BadRequestException("Comment content is required");
        }

        if (request.getContent().length() < 10) {
            throw new BadRequestException("Comment must be at least 10 characters");
        }

        if (request.getRating() == null) {
            throw new BadRequestException("Rating is required");
        }

        if (request.getRating().compareTo(BigDecimal.ONE) < 0 ||
                request.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BadRequestException("Rating must be between 1.0 and 5.0");
        }

        log.debug("✅ Validation passed");
    }

    private CommentsPageResponse.RatingDistribution getRatingDistribution(Long postId) {
        List<Object[]> distribution = commentRepository.getRatingDistribution(postId);

        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : distribution) {
            BigDecimal rating = (BigDecimal) row[0];
            Long count = (Long) row[1];
            map.put(rating.intValue(), count);
        }

        return CommentsPageResponse.RatingDistribution.builder()
                .star5(map.getOrDefault(5, 0L))
                .star4(map.getOrDefault(4, 0L))
                .star3(map.getOrDefault(3, 0L))
                .star2(map.getOrDefault(2, 0L))
                .star1(map.getOrDefault(1, 0L))
                .build();
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .rating(comment.getRating())
                .helpfulCount(comment.getHelpfulCount())
                .isVerifiedBooking(comment.getIsVerifiedBooking())
                // User info
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .userEmail(comment.getUser().getEmail())
                .userAvatar(comment.getUser().getAvatarUrl())
                // Post info
                .postId(comment.getPost().getId())
                .postTitle(comment.getPost().getTitle())
                // Timestamps
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                // Formatted
                .formattedDate(formatDate(comment.getCreatedAt()))
                .formattedRating(formatRating(comment.getRating()))
                .build();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(dateTime, now).toDays();

        if (days == 0) {
            long hours = java.time.Duration.between(dateTime, now).toHours();
            if (hours == 0) {
                long minutes = java.time.Duration.between(dateTime, now).toMinutes();
                return minutes == 0 ? "Vừa xong" : minutes + " phút trước";
            }
            return hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    private String formatRating(BigDecimal rating) {
        return rating.setScale(1, RoundingMode.HALF_UP).toString() + " ⭐";
    }
}

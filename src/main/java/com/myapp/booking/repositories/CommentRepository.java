package com.myapp.booking.repositories;

import com.myapp.booking.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by post
    Page<Comment> findByPostIdAndIsActiveTrue(Long postId, Pageable pageable);

    List<Comment> findByPostIdAndIsActiveTrue(Long postId);

    // Find comment by ID
    Optional<Comment> findByIdAndIsActiveTrue(Long id);

    // Check if user already commented on post
    boolean existsByPostIdAndUserIdAndIsActiveTrue(Long postId, Long userId);

    // Get user's comment on post
    Optional<Comment> findByPostIdAndUserIdAndIsActiveTrue(Long postId, Long userId);

    // Count comments for post
    Long countByPostIdAndIsActiveTrue(Long postId);

    // Calculate average rating
    @Query("SELECT AVG(c.rating) FROM Comment c WHERE c.post.id = :postId AND c.isActive = true")
    BigDecimal calculateAverageRating(@Param("postId") Long postId);

    // Get rating distribution
    @Query("SELECT c.rating, COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isActive = true GROUP BY c.rating")
    List<Object[]> getRatingDistribution(@Param("postId") Long postId);

    // Get comments by user
    Page<Comment> findByUserIdAndIsActiveTrue(Long userId, Pageable pageable);
}
package com.myapp.booking.repositories;

import com.myapp.booking.models.CommentHelpful;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentHelpfulRepository extends JpaRepository<CommentHelpful, Long> {

    // Check if user marked comment as helpful
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    // Find by comment and user
    Optional<CommentHelpful> findByCommentIdAndUserId(Long commentId, Long userId);

    // Count helpful marks for a comment
    long countByCommentId(Long commentId);

    // Find all helpful marks by user
    List<CommentHelpful> findByUserId(Long userId);

    // Find all helpful marks for a comment
    List<CommentHelpful> findByCommentId(Long commentId);

    // Delete by comment and user
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}


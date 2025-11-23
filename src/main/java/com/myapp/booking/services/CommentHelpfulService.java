package com.myapp.booking.services;

import com.myapp.booking.models.CommentHelpful;
import com.myapp.booking.models.Comment;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.CommentHelpfulRepository;
import com.myapp.booking.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentHelpfulService {

    private final CommentHelpfulRepository commentHelpfulRepository;
    private final CommentRepository commentRepository;

    /**
     * Mark comment as helpful
     */
    @Transactional
    public void markAsHelpful(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!commentHelpfulRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            CommentHelpful helpful = CommentHelpful.builder()
                    .comment(comment)
                    .user(user)
                    .build();

            commentHelpfulRepository.save(helpful);

            // Update helpful count
            comment.incrementHelpfulCount();
            commentRepository.save(comment);
        }
    }

    /**
     * Unmark comment as helpful
     */
    @Transactional
    public void unmarkAsHelpful(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (commentHelpfulRepository.existsByCommentIdAndUserId(commentId, user.getId())) {
            commentHelpfulRepository.deleteByCommentIdAndUserId(commentId, user.getId());

            // Update helpful count
            comment.decrementHelpfulCount();
            commentRepository.save(comment);
        }
    }

    /**
     * Check if user marked comment as helpful
     */
    public boolean isMarkedAsHelpful(Long commentId, Long userId) {
        return commentHelpfulRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    /**
     * Get helpful count for comment
     */
    public long getHelpfulCount(Long commentId) {
        return commentHelpfulRepository.countByCommentId(commentId);
    }
}


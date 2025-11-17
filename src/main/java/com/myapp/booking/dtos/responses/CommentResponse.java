package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private BigDecimal rating;
    private List<String> images;
    private Integer helpfulCount;
    private Boolean isVerifiedBooking;

    // User info
    private Long userId;
    private String userName;
    private String userEmail;
    private String userAvatar;

    // Post info
    private Long postId;
    private String postTitle;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Formatted
    private String formattedDate;
    private String formattedRating;
}
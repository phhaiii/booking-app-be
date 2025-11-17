package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentsPageResponse {

    private List<CommentResponse> comments;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Boolean hasNext;

    // Statistics
    private BigDecimal averageRating;
    private Long totalComments;
    private RatingDistribution ratingDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingDistribution {
        private Long star5;
        private Long star4;
        private Long star3;
        private Long star2;
        private Long star1;
    }
}
package com.myapp.booking.dtos.responses.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostStatisticsResponse {
    private Long postId;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long bookingCount;
}
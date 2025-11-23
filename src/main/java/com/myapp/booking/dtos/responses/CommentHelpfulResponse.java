package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentHelpfulResponse {
    private Long commentId;
    private Long helpfulCount;
    private Boolean isMarkedByCurrentUser;
}


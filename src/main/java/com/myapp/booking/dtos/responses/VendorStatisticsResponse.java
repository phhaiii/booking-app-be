package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorStatisticsResponse {
    private Long vendorId;
    private Long totalPosts;
    private Long publishedPosts;
    private Long draftPosts;
}
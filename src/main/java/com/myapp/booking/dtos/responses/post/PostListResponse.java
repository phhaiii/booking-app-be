package com.myapp.booking.dtos.responses.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.booking.models.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListResponse {

    private Long id;
    private String title;
    private String description;
    private String location;
    private BigDecimal price;
    private Integer capacity;
    private String style;
    private String thumbnailImage; // First image
    private Integer imageCount;
    private String status;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long bookingCount;

    private String vendorName;
    private String vendorAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    public static PostListResponse fromEntity(Post post) {
        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .location(post.getLocation())
                .price(post.getPrice())
                .capacity(post.getCapacity())
                .style(post.getStyle())
                .thumbnailImage(post.getImages().isEmpty() ? null : post.getImages().get(0))
                .imageCount(post.getImages().size())
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .bookingCount(post.getBookingCount())
                .vendorName(post.getVendor().getFullName())
                .vendorAvatar(post.getVendor().getAvatarUrl())
                .createdAt(post.getCreatedAt())
                .publishedAt(post.getPublishedAt())
                .build();
    }
}
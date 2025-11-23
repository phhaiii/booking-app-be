package com.myapp.booking.dtos.responses.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.booking.models.Post;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private String thumbnailImage;
    private Integer imageCount;
    private List<String> images;
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

    // Helper method to convert a single image filename to URL
    private static String convertImageUrl(String image) {
        if (image == null || image.isEmpty()) {
            return image;
        }
        // If already a full URL, return as is
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image;
        }
        // If already has /uploads/ prefix, return as is
        if (image.startsWith("/uploads/")) {
            return image;
        }
        // Otherwise, add /uploads/ prefix
        return "/uploads/" + image;
    }

    // Helper method to convert list of image filenames to URLs
    private static List<String> convertImageUrls(List<String> images) {
        if (images == null || images.isEmpty()) {
            return images;
        }

        return images.stream()
                .map(PostListResponse::convertImageUrl)
                .collect(Collectors.toList());
    }

    public static PostListResponse fromEntity(Post post) {
        List<String> convertedImages = convertImageUrls(post.getImages());

        return PostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .location(post.getLocation())
                .price(post.getPrice())
                .capacity(post.getCapacity())
                .style(post.getStyle())
                .thumbnailImage(convertedImages.isEmpty() ? null : convertedImages.get(0))
                .imageCount(convertedImages.size())
                .images(convertedImages)
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

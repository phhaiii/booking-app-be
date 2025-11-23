package com.myapp.booking.dtos.responses.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.booking.models.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String location;
    private BigDecimal price;
    private Integer capacity;
    private String style;
    private List<String> images;
    private Set<String> amenities;
    private Boolean allowComments;
    private Boolean enableNotifications;
    private String status;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long bookingCount;

    // Vendor info
    private VendorInfo vendor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VendorInfo {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private String avatarUrl;
    }

    // Helper method to convert filenames to full URLs
    private static List<String> convertImageUrls(List<String> images) {
        if (images == null || images.isEmpty()) {
            return images;
        }

        return images.stream()
                .map(image -> {
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
                })
                .collect(Collectors.toList());
    }

    // Static factory method
    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .location(post.getLocation())
                .price(post.getPrice())
                .capacity(post.getCapacity())
                .style(post.getStyle())
                .images(convertImageUrls(post.getImages()))
                .amenities(post.getAmenities())
                .allowComments(post.getAllowComments())
                .enableNotifications(post.getEnableNotifications())
                .status(post.getStatus().name())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .bookingCount(post.getBookingCount())
                .vendor(VendorInfo.builder()
                        .id(post.getVendor().getId())
                        .fullName(post.getVendor().getFullName())
                        .email(post.getVendor().getEmail())
                        .phone(post.getVendor().getPhone())
                        .avatarUrl(post.getVendor().getAvatarUrl())
                        .build())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .publishedAt(post.getPublishedAt())
                .build();
    }
}
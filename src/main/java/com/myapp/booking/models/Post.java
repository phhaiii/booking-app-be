package com.myapp.booking.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "style", length = 50)
    private String style;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "rating", precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "working_days", columnDefinition = "LONGTEXT")
    private String workingDays; // JSON array of working days

    @Column(name = "available_slots")
    @Builder.Default
    private Integer availableSlots = 4; // 4 slots per day (default)

    // ✅ Quan hệ OneToMany với post_images
    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    // ✅ Quan hệ OneToMany với post_amenities
    @ElementCollection
    @CollectionTable(name = "post_amenities", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "amenity")
    @Builder.Default
    private Set<String> amenities = new HashSet<>();

    @Column(name = "allow_comments", nullable = false)
    @Builder.Default
    private Boolean allowComments = true;

    @Column(name = "enable_notifications", nullable = false)
    @Builder.Default
    private Boolean enableNotifications = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "booking_count", nullable = false)
    @Builder.Default
    private Long bookingCount = 0L;

    // ✅ Quan hệ ManyToOne với User (Vendor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ✅ Quan hệ OneToMany với Menu
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Menu> menus = new ArrayList<>();


    // Enum for Post Status
    public enum PostStatus {
        PENDING,
        PUBLISHED,
        REJECTED,
        DRAFT
    }

    // Helper methods
    @PrePersist
    protected void onCreate() {
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
        if (isActive == null) isActive = true;
        if (isDeleted == null) isDeleted = false;
        if (allowComments == null) allowComments = true;
        if (enableNotifications == null) enableNotifications = true;
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    // ❌ XÓA METHOD NÀY - không cần thiết
    // public void setVendorFromPost() {
    //     if (this.post != null && this.post.getVendor() != null) {
    //         this.vendor = this.post.getVendor();
    //     }
    // }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void incrementBookingCount() {
        this.bookingCount++;
    }

    public Long getVendorId() {
        return vendor != null ? vendor.getId() : null;
    }
}
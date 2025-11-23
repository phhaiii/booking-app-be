package com.myapp.booking.models;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_booking_code", columnList = "booking_code"),
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_vendor_id", columnList = "vendor_id"),
                @Index(name = "idx_venue_id", columnList = "venue_id"),
                @Index(name = "idx_menu_id", columnList = "menu_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_booking_slot", columnList = "post_id, booking_date, slot_index")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", unique = true, nullable = false, length = 20)
    private String bookingCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 255)
    private String customerPhone;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "booking_date", nullable = false)
    private java.sql.Date bookingDate;

    @Column(name = "start_time", nullable = false)
    private java.sql.Time startTime;

    @Column(name = "end_time", nullable = false)
    private java.sql.Time endTime;

    @Column(name = "slot_index")
    private Integer slotIndex; // 0=10-12h, 1=12-14h, 2=14-16h, 3=16-18h

    @Column(name = "duration_hours")
    private Double durationHours;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(name = "number_of_tables")
    private Integer numberOfTables;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Column(name = "discount_amount")
    private Double discountAmount;

    @Column(name = "final_amount", nullable = false)
    private Double finalAmount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "additional_services", columnDefinition = "TEXT")
    private String additionalServices;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (this.bookingCode == null) {
            this.bookingCode = generateUniqueBookingCode();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateUniqueBookingCode() {
        // Format: BK-YYYYMMDD-XXXX (e.g., BK-20241116-A5F3)
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "BK-" + datePart + "-" + randomPart;
    }
}
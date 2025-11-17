package com.myapp.booking.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
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


    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @Column(name = "booking_date", nullable = false)
    private java.sql.Date bookingDate;

    @Column(name = "start_time", nullable = false)
    private java.sql.Time startTime;

    @Column(name = "end_time", nullable = false)
    private java.sql.Time endTime;

    @Column(name = "slot_index")
    private Integer slotIndex;

    @Column(name = "duration_hours")
    private Double durationHours;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests = 1;

    @Column(name = "number_of_tables")
    private Integer numberOfTables;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "deposit_amount")
    private Double depositAmount = 0.00;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.00;

    @Column(name = "final_amount", nullable = false)
    private Double finalAmount;

    @Column(name = "currency", length = 3)
    private String currency = "VND";

    @Column(name = "additional_services", columnDefinition = "JSON")
    private String additionalServices;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "refund_amount")
    private Double refundAmount = 0.00;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
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
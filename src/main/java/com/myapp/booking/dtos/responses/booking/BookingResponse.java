package com.myapp.booking.dtos.responses.booking;

import com.myapp.booking.models.WeddingBooking;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long venueId;
    private String venueName;
    private String serviceName;
    private WeddingBooking.ServiceType serviceType;
    private String serviceTypeDisplay;
    private LocalDateTime requestedDate;
    private Integer numberOfGuests;
    private Double budget;
    private String message;
    private WeddingBooking.BookingStatus status;
    private String statusDisplay;
    private String rejectionReason;
    private Long vendorId;
    private String vendorName;
    private Long userId;
    private String userName;
    private LocalDateTime confirmedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String timeAgo;
    private Boolean isOverdue;

    public static BookingResponse fromEntity(WeddingBooking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .customerEmail(booking.getCustomerEmail())
                .venueId(booking.getVenueId())
                .venueName(booking.getVenue() != null ? booking.getVenue().getName() : null)
                .serviceName(booking.getServiceName())
                .serviceType(booking.getServiceType())
                .serviceTypeDisplay(booking.getServiceType().getDisplayName())
                .requestedDate(booking.getRequestedDate())
                .numberOfGuests(booking.getNumberOfGuests())
                .budget(booking.getBudget())
                .message(booking.getMessage())
                .status(booking.getStatus())
                .statusDisplay(booking.getStatus().getDisplayName())
                .rejectionReason(booking.getRejectionReason())
                .vendorId(booking.getVendorId())
                .vendorName(booking.getVendor() != null ? booking.getVendor().getFullName() : null)
                .userId(booking.getUserId())
                .userName(booking.getUser() != null ? booking.getUser().getFullName() : null)
                .confirmedAt(booking.getConfirmedAt())
                .rejectedAt(booking.getRejectedAt())
                .completedAt(booking.getCompletedAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .timeAgo(calculateTimeAgo(booking.getCreatedAt()))
                .isOverdue(checkOverdue(booking))
                .build();
    }

    private static String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (minutes < 1440) {
            return (minutes / 60) + " giờ trước";
        } else {
            return (minutes / 1440) + " ngày trước";
        }
    }

    private static Boolean checkOverdue(WeddingBooking booking) {
        if (booking.getRequestedDate() == null ||
                booking.getStatus() != WeddingBooking.BookingStatus.PENDING) {
            return false;
        }
        return LocalDateTime.now().isAfter(booking.getRequestedDate());
    }
}
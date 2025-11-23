package com.myapp.booking.dtos.responses;


import com.myapp.booking.models.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long postId;
    private Long venueId;
    private Long vendorId;
    private String venueName;
    private String venueImage;
    private java.sql.Date bookingDate;
    private java.sql.Time startTime;
    private java.sql.Time endTime;
    private Integer slotIndex;
    private Double durationHours;
    private Integer numberOfGuests;
    private Integer numberOfTables;
    private Double unitPrice;
    private Double totalAmount;
    private Double depositAmount;
    private Double discountAmount;
    private Double finalAmount;
    private String currency;
    private String additionalServices;
    private String specialRequests;
    private String notes;
    private String status;

    // Cancellation/Rejection info
    private Long cancelledBy;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    // Confirmation info
    private Long confirmedBy;
    private LocalDateTime confirmedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Convert from Entity to DTO
    public static BookingResponse fromEntity(Booking booking) {
        // Ensure venueId is set - fallback to postId if null (for legacy bookings)
        Long venueId = booking.getVenueId() != null ? booking.getVenueId() : booking.getPostId();
        
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .customerEmail(booking.getCustomerEmail())
                .postId(booking.getPostId())
                .venueId(venueId)
                .vendorId(booking.getVendorId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .slotIndex(booking.getSlotIndex() != null ? booking.getSlotIndex() : computeSlotFromStartTime(booking.getStartTime()))
                .durationHours(booking.getDurationHours())
                .numberOfGuests(booking.getNumberOfGuests())
                .numberOfTables(booking.getNumberOfTables())
                .unitPrice(booking.getUnitPrice())
                .totalAmount(booking.getTotalAmount())
                .depositAmount(booking.getDepositAmount())
                .discountAmount(booking.getDiscountAmount())
                .finalAmount(booking.getFinalAmount())
                .currency(booking.getCurrency())
                .additionalServices(booking.getAdditionalServices())
                .specialRequests(booking.getSpecialRequests())
                .notes(booking.getNotes())
                .status(booking.getStatus())
                .cancelledBy(booking.getCancelledBy())
                .cancelledAt(booking.getCancelledAt())
                .cancellationReason(booking.getCancellationReason())
                .confirmedBy(booking.getConfirmedBy())
                .confirmedAt(booking.getConfirmedAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();

    }

    /**
     * Compute slot index from start time for existing bookings
     */
    private static Integer computeSlotFromStartTime(java.sql.Time startTime) {
        if (startTime == null) {
            return 0; // Default to first slot
        }

        java.time.LocalTime time = startTime.toLocalTime();

        // Map to predefined slots based on start time
        if (time.equals(java.time.LocalTime.of(10, 0))) {
            return 0; // 10-12 slot
        } else if (time.equals(java.time.LocalTime.of(12, 0))) {
            return 1; // 12-14 slot
        } else if (time.equals(java.time.LocalTime.of(14, 0))) {
            return 2; // 14-16 slot
        } else if (time.equals(java.time.LocalTime.of(16, 0))) {
            return 3; // 16-18 slot
        } else {
            // Fallback: find closest slot based on time
            if (time.isBefore(java.time.LocalTime.of(12, 0))) {
                return 0;
            } else if (time.isBefore(java.time.LocalTime.of(14, 0))) {
                return 1;
            } else if (time.isBefore(java.time.LocalTime.of(16, 0))) {
                return 2;
            } else {
                return 3;
            }
        }
    }

}

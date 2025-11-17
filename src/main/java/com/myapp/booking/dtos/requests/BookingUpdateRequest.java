package com.myapp.booking.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingUpdateRequest {

    @Future(message = "Booking date must be in the future")
    private LocalDateTime bookingDate;

    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @Size(max = 1000, message = "Note cannot exceed 1000 characters")
    private String note;

    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    private Long menuId;
}

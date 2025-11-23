package com.myapp.booking.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @JsonAlias({"venueId"})
    private Long postId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 20, message = "Customer phone cannot exceed 20 characters")
    private String customerPhone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Customer email cannot exceed 100 characters")
    private String customerEmail;

    // Either bookingDate or bookingDateTime is acceptable
    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private java.sql.Date bookingDate;

    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime bookingDateTime;

    // NEW: Slot index for predefined time slots: 0=10-12h, 1=12-14h, 2=14-16h, 3=16-18h
    // Optional for backward compatibility - if not provided, will be derived from startTime
    @Min(value = 0, message = "Slot index must be between 0 and 3")
    @Max(value = 3, message = "Slot index must be between 0 and 3")
    private Integer slotIndex;

    private String startTime;

    private String endTime;

    @JsonAlias({"numberOfGuests", "number_of_guests", "guestCount"})
    private Integer numberOfGuests;

    // Optional: if null, service will use post.price
    @DecimalMin(value = "0.0", message = "Unit price must be positive")
    private Double unitPrice;

    @DecimalMin(value = "0.0", message = "Deposit amount must be non-negative")
    private Double depositAmount = 0.00;

    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private Double discountAmount = 0.00;

    private String additionalServices;

    @JsonAlias({"specialRequests", "special_requests"})
    @Size(max = 1000, message = "Special requests cannot exceed 1000 characters")
    private String specialRequests;

    // BACKWARD COMPATIBILITY: Optional duration hint for old API clients
    @Min(1)
    private Integer durationMinutes;
}

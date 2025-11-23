package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for booking confirmation that includes both booking details and updated slot availability
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfirmResponse {
    private BookingResponse booking;
    private SlotAvailabilityResponse slotAvailability;
}


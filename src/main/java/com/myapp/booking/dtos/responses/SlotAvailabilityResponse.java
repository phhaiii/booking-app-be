package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotAvailabilityResponse {
    private Long postId;
    private String postTitle;
    private String bookingDate;
    private Integer totalSlots;
    private Integer availableSlots;
    private Integer bookedSlots;
    private List<SlotInfo> slots; // Changed from timeSlots to slots

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotInfo {
        private Integer slotIndex; // 0=10-12h, 1=12-14h, 2=14-16h, 3=16-18h
        private LocalTime startTime;
        private LocalTime endTime;
        private String displayText; // e.g., "10:00 - 12:00"
        private boolean isAvailable;
        private String status; // AVAILABLE, BOOKED
    }
}


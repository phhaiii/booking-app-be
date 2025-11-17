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
    private List<TimeSlot> timeSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private String slotId;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean isAvailable;
        private String status; // AVAILABLE, BOOKED
    }
}


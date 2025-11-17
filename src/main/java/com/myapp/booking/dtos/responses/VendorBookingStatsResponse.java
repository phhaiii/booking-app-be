package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorBookingStatsResponse {
    private long totalBookings;
    private long pendingCount;
    private long confirmedCount;
    private long cancelledCount;
    private long completedCount;
    private long upcomingCount; // future dates
    private long todayCount;    // bookings happening today
    private double totalRevenue; // sum of finalAmount for confirmed + completed
}


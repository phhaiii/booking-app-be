package com.myapp.booking.dtos.responses.booking;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatisticsResponse {

    private Long totalBookings;
    private Long pendingBookings;
    private Long confirmedBookings;
    private Long rejectedBookings;
    private Long completedBookings;
    private Long cancelledBookings;
    private Long overdueBookings;
    private Double totalRevenue;
    private Double pendingRevenue;
    private Double confirmedRevenue;

    // Statistics by service type
    private Long venueBookings;
    private Long photographyBookings;
    private Long cateringBookings;
    private Long decorationBookings;
    private Long fashionBookings;

    // Time-based statistics
    private Long todayBookings;
    private Long thisWeekBookings;
    private Long thisMonthBookings;
}
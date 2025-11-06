package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.booking.BookingActionRequest;
import com.myapp.booking.dtos.requests.booking.BookingRequest;
import com.myapp.booking.dtos.requests.booking.BookingUpdateRequest;
import com.myapp.booking.dtos.responses.booking.BookingResponse;
import com.myapp.booking.dtos.responses.booking.BookingStatisticsResponse;
import com.myapp.booking.models.WeddingBooking;
import java.time.LocalDateTime;
import java.util.List;

public interface IWeddingBookingService {

    // CRUD operations - SỬA: userId từ int -> Long
    BookingResponse createBooking(BookingRequest request, Long userId);
    BookingResponse updateBooking(Long id, BookingUpdateRequest request, Long vendorId);
    BookingResponse getBookingById(Long id, Long vendorId);
    List<BookingResponse> getAllBookingsByVendor(Long vendorId);
    void deleteBooking(Long id, Long vendorId);

    // Status operations
    List<BookingResponse> getBookingsByStatus(Long vendorId, WeddingBooking.BookingStatus status);
    BookingResponse confirmBooking(Long id, Long vendorId, BookingActionRequest request);
    BookingResponse rejectBooking(Long id, Long vendorId, BookingActionRequest request);
    BookingResponse completeBooking(Long id, Long vendorId);
    BookingResponse cancelBooking(Long id, Long userId);

    // Filter and search
    List<BookingResponse> getBookingsByDateRange(Long vendorId, LocalDateTime startDate, LocalDateTime endDate);
    List<BookingResponse> getBookingsByServiceType(Long vendorId, WeddingBooking.ServiceType serviceType);
    List<BookingResponse> searchBookings(Long vendorId, String keyword);
    List<BookingResponse> getOverdueBookings(Long vendorId);

    // User bookings
    List<BookingResponse> getBookingsByUser(Long userId);

    // Venue bookings
    List<BookingResponse> getBookingsByVenue(Long venueId);

    // Statistics
    BookingStatisticsResponse getStatistics(Long vendorId);
    Long getPendingCount(Long vendorId);
    Long getConfirmedCount(Long vendorId);
    Long getRejectedCount(Long vendorId);

    // Time-based queries
    List<BookingResponse> getTodayBookings(Long vendorId);
    List<BookingResponse> getThisWeekBookings(Long vendorId);
    List<BookingResponse> getThisMonthBookings(Long vendorId);

    // Validation
    boolean isTimeSlotAvailable(Long venueId, LocalDateTime requestedDate);
}
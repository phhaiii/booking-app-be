package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.BookingRequest;
import com.myapp.booking.dtos.responses.BookingResponse;
import com.myapp.booking.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IBookingService {

    // CRUD operations
    BookingResponse createBooking(BookingRequest request, UserPrincipal currentUser);
    BookingResponse getBookingById(Long bookingId, UserPrincipal currentUser);
    Page<BookingResponse> getUserBookings(Long userId, Pageable pageable);
    void deleteBooking(Long id, UserPrincipal currentUser);

    // Status operations
    Page<BookingResponse> getBookingsByStatus(Long vendorId, String status, Pageable pageable);
    BookingResponse confirmBooking(Long bookingId, UserPrincipal currentUser);
    BookingResponse completeBooking(Long bookingId, UserPrincipal currentUser);
    BookingResponse cancelBooking(Long bookingId, UserPrincipal currentUser);
    BookingResponse rejectBooking(Long bookingId, String reason, UserPrincipal currentUser);

    // Vendor bookings
    Page<BookingResponse> getVendorBookings(Long vendorId, Pageable pageable, UserPrincipal currentUser);


    // Validation
    boolean isTimeSlotAvailable(Long venueId, LocalDateTime requestedDate);

    // Alias used by controller
    boolean checkAvailability(Long venueId, LocalDateTime requestedDate);
}
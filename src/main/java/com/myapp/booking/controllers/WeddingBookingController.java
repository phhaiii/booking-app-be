package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.booking.BookingActionRequest;
import com.myapp.booking.dtos.requests.booking.BookingRequest;
import com.myapp.booking.dtos.requests.booking.BookingUpdateRequest;
import com.myapp.booking.dtos.responses.booking.BookingResponse;
import com.myapp.booking.dtos.responses.booking.BookingStatisticsResponse;
import com.myapp.booking.models.WeddingBooking;
import com.myapp.booking.security.CurrentUser;
import com.myapp.booking.security.UserPrincipal;
import com.myapp.booking.services.interfaces.IWeddingBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "Wedding Booking Management APIs")
public class WeddingBookingController {

    private final IWeddingBookingService bookingService;

    @PostMapping
    @Operation(summary = "Create new booking", description = "Create a new booking request")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse created = bookingService.createBooking(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/vendor")
    @Operation(summary = "Get all bookings for vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getAllBookingsForVendor(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getAllBookingsByVendor(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/{id}")
    @Operation(summary = "Get booking by ID for vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.getBookingById(id, currentUser.getId());
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/vendor/{id}")
    @Operation(summary = "Update booking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse updated = bookingService.updateBooking(id, request, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/vendor/{id}")
    @Operation(summary = "Delete booking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        bookingService.deleteBooking(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vendor/status/{status}")
    @Operation(summary = "Get bookings by status")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(
            @PathVariable WeddingBooking.BookingStatus status,
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(currentUser.getId(), status);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/vendor/{id}/confirm")
    @Operation(summary = "Confirm booking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable Long id,
            @RequestBody(required = false) BookingActionRequest request,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse confirmed = bookingService.confirmBooking(id, currentUser.getId(), request);
        return ResponseEntity.ok(confirmed);
    }

    @PatchMapping("/vendor/{id}/reject")
    @Operation(summary = "Reject booking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingActionRequest request,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse rejected = bookingService.rejectBooking(id, currentUser.getId(), request);
        return ResponseEntity.ok(rejected);
    }

    @PatchMapping("/vendor/{id}/complete")
    @Operation(summary = "Complete booking")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse completed = bookingService.completeBooking(id, currentUser.getId());
        return ResponseEntity.ok(completed);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking (by user)")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse cancelled = bookingService.cancelBooking(id, currentUser.getId());
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/vendor/date-range")
    @Operation(summary = "Get bookings by date range")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getBookingsByDateRange(
                currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/service-type/{serviceType}")
    @Operation(summary = "Get bookings by service type")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByServiceType(
            @PathVariable WeddingBooking.ServiceType serviceType,
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getBookingsByServiceType(
                currentUser.getId(), serviceType);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/search")
    @Operation(summary = "Search bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> searchBookings(
            @RequestParam String keyword,
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.searchBookings(currentUser.getId(), keyword);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/overdue")
    @Operation(summary = "Get overdue bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getOverdueBookings(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getOverdueBookings(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/my-bookings")
    @Operation(summary = "Get user's bookings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getBookingsByUser(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/venue/{venueId}")
    @Operation(summary = "Get bookings by venue")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR')")
    public ResponseEntity<List<BookingResponse>> getBookingsByVenue(@PathVariable Long venueId) {
        List<BookingResponse> bookings = bookingService.getBookingsByVenue(venueId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/statistics")
    @Operation(summary = "Get booking statistics")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<BookingStatisticsResponse> getStatistics(
            @CurrentUser UserPrincipal currentUser) {
        BookingStatisticsResponse stats = bookingService.getStatistics(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/vendor/count/pending")
    @Operation(summary = "Get pending bookings count")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Long> getPendingCount(@CurrentUser UserPrincipal currentUser) {
        Long count = bookingService.getPendingCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/vendor/count/confirmed")
    @Operation(summary = "Get confirmed bookings count")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Long> getConfirmedCount(@CurrentUser UserPrincipal currentUser) {
        Long count = bookingService.getConfirmedCount(currentUser.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/vendor/today")
    @Operation(summary = "Get today's bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getTodayBookings(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getTodayBookings(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/this-week")
    @Operation(summary = "Get this week's bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getThisWeekBookings(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getThisWeekBookings(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/vendor/this-month")
    @Operation(summary = "Get this month's bookings")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<BookingResponse>> getThisMonthBookings(
            @CurrentUser UserPrincipal currentUser) {
        List<BookingResponse> bookings = bookingService.getThisMonthBookings(currentUser.getId());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Check time slot availability")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR')")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestedDate) {
        boolean available = bookingService.isTimeSlotAvailable(venueId, requestedDate);
        return ResponseEntity.ok(available);
    }
}
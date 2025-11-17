package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.BookingRequest;
import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.responses.BookingResponse;
import com.myapp.booking.dtos.responses.VendorBookingStatsResponse;
import com.myapp.booking.repositories.BookingRepository;
import com.myapp.booking.security.CurrentUser;
import com.myapp.booking.security.UserPrincipal;
import com.myapp.booking.services.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    /**
     * Create new booking
     * POST /api/bookings
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @CurrentUser UserPrincipal currentUser) {

        log.info("POST /api/bookings - Creating booking for user: {}", currentUser.getId());

        BookingResponse booking = bookingService.createBooking(request, currentUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(booking, "Booking created successfully"));
    }

    /**
     * Get current user's bookings
     * GET /api/bookings/user/my-bookings
     */
    @GetMapping("/user/my-bookings")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/bookings/user/my-bookings - User: {}", currentUser.getId());

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookingResponse> bookings = bookingService.getUserBookings(
                currentUser.getId(),
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    /**
     * Get booking by ID (numeric only to avoid collision with '/vendor/...')
     * GET /api/bookings/{id}
     */
    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/{} - User: {}", id, currentUser.getId());

        BookingResponse booking = bookingService.getBookingById(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking retrieved successfully"));
    }

    /**
     * Check basic availability for a venue on a specific date
     * Returns true if the venue has at least one slot available
     *
     * @param postId The ID of the post/venue to check
     * @param date The date to check availability (format: yyyy-MM-dd)
     * @param currentUser The authenticated user
     * @return Boolean indicating if venue is available
     *
     * Example: GET /api/bookings/availability?postId=1&date=2025-12-25
     */
    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @RequestParam Long postId,
            @RequestParam String date,
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/availability - postId: {}, date: {}", postId, date);

        try {
            LocalDateTime dateTime;
            LocalDate requestedDate;

            // Try to parse as LocalDateTime first (ISO format: 2025-11-20T10:00:00)
            // If that fails, try parsing as LocalDate (yyyy-MM-dd)
            try {
                dateTime = LocalDateTime.parse(date);
                requestedDate = dateTime.toLocalDate();
            } catch (DateTimeParseException e1) {
                // Try parsing as LocalDate only
                requestedDate = LocalDate.parse(date);
                dateTime = requestedDate.atStartOfDay();
            }

            // Validate date is not in the past
            if (requestedDate.isBefore(LocalDate.now())) {
                return ResponseEntity.ok(ApiResponse.success(false,
                    "Cannot check availability for past dates"));
            }

            // Check availability
            boolean isAvailable = bookingService.checkAvailability(postId, dateTime);

            return ResponseEntity.ok(ApiResponse.success(isAvailable,
                    isAvailable ? "Venue has available slots" : "Venue is fully booked"));

        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", date, e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date format. Please use yyyy-MM-dd or ISO datetime format (e.g., 2025-12-25 or 2025-12-25T10:00:00)"));
        } catch (Exception e) {
            log.error("Error checking availability for postId: {}, date: {}", postId, date, e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("Error checking availability: " + e.getMessage()));
        }
    }

    /**
     * Get available time slots for booking
     * Returns the 4 predefined time slots with their availability status
     *
     * @param postId The ID of the post/venue
     * @param date The date to check (format: yyyy-MM-dd)
     * @return List of time slots with availability information
     *
     * Example: GET /api/bookings/time-slots?postId=1&date=2025-12-25
     */
    @GetMapping("/time-slots")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<TimeSlotInfo>>> getTimeSlots(
            @RequestParam Long postId,
            @RequestParam String date,
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/time-slots - postId: {}, date: {}", postId, date);

        try {
            // Validate and parse date
            LocalDate requestedDate = LocalDate.parse(date);

            // Validate date is not in the past
            if (requestedDate.isBefore(LocalDate.now())) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Cannot get time slots for past dates"));
            }

            // Convert to java.sql.Date for service layer
            java.sql.Date bookingDate = java.sql.Date.valueOf(requestedDate);

            // Get time slots availability
            java.util.List<TimeSlotInfo> timeSlots = getTimeSlotsInfo(postId, bookingDate);

            return ResponseEntity.ok(ApiResponse.success(timeSlots, "Time slots retrieved successfully"));

        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", date, e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-12-25)"));
        } catch (Exception e) {
            log.error("Error getting time slots for postId: {}, date: {}", postId, date, e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("Error retrieving time slots: " + e.getMessage()));
        }
    }

    /**
     * Helper method to get time slots information
     */
    private java.util.List<TimeSlotInfo> getTimeSlotsInfo(Long postId, java.sql.Date bookingDate) {
        java.util.List<TimeSlotInfo> timeSlots = new java.util.ArrayList<>();

        // Get all predefined time slots
        for (com.myapp.booking.enums.TimeSlot slot : com.myapp.booking.enums.TimeSlot.values()) {
            // Check if this slot is available
            boolean isAvailable = !bookingRepository.existsByPostIdAndBookingDateAndSlotIndex(
                    postId, bookingDate, slot.getIndex());

            TimeSlotInfo slotInfo = TimeSlotInfo.builder()
                    .slotIndex(slot.getIndex())
                    .startTime(slot.getStartTime().toString())
                    .endTime(slot.getEndTime().toString())
                    .displayText(slot.getDisplayText())
                    .isAvailable(isAvailable)
                    .build();

            timeSlots.add(slotInfo);
        }

        return timeSlots;
    }

    /**
     * Inner class for time slot information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TimeSlotInfo {
        private Integer slotIndex;
        private String startTime;
        private String endTime;
        private String displayText;
        private Boolean isAvailable;
    }

    /**
     * Get detailed slot availability information for a venue on a specific date
     * Returns total slots, available slots, booked slots, and detailed time slot information
     *
     * @param postId The ID of the post/venue to check
     * @param date The date to check (format: yyyy-MM-dd)
     * @param currentUser The authenticated user
     * @return Detailed slot availability information including time slots
     *
     * Example: GET /api/bookings/slot-availability?postId=1&date=2025-12-25
     *
     * Response includes:
     * - totalSlots: Total number of slots available per day (default: 4)
     * - availableSlots: Number of slots still available
     * - bookedSlots: Number of slots already booked
     * - timeSlots: Array of time slots with availability status
     *   - MORNING: 10:00-14:00
     *   - AFTERNOON: 14:00-18:00
     */
    @GetMapping("/slot-availability")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<com.myapp.booking.dtos.responses.SlotAvailabilityResponse>> getSlotAvailability(
            @RequestParam Long postId,
            @RequestParam String date,
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/slot-availability - postId: {}, date: {}", postId, date);

        try {
            // Validate and parse date
            LocalDate requestedDate = LocalDate.parse(date);

            // Validate date is not in the past
            if (requestedDate.isBefore(LocalDate.now())) {
                return ResponseEntity
                        .badRequest()
                        .body(ApiResponse.error("Cannot check availability for past dates"));
            }

            // Convert to java.sql.Date for service layer
            java.sql.Date bookingDate = java.sql.Date.valueOf(requestedDate);

            // Get detailed slot availability
            var slotAvailability = bookingService.getSlotAvailability(postId, bookingDate);

            return ResponseEntity.ok(ApiResponse.success(slotAvailability,
                    "Slot availability retrieved successfully"));

        } catch (DateTimeParseException e) {
            log.error("Invalid date format: {}", date, e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date format. Please use yyyy-MM-dd format (e.g., 2025-12-25)"));
        } catch (IllegalArgumentException e) {
            log.error("Invalid date value: {}", date, e);
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date value: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting slot availability for postId: {}, date: {}", postId, date, e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error("Error retrieving slot availability: " + e.getMessage()));
        }
    }



    /**
     * Cancel booking
     * POST /api/bookings/{id}/cancel
     */
    @PostMapping("/{id:\\d+}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("POST /api/bookings/{}/cancel - User: {}", id, currentUser.getId());

        BookingResponse booking = bookingService.cancelBooking(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled successfully"));
    }

    /**
     * Delete booking
     * DELETE /api/bookings/{id}
     */
    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("DELETE /api/bookings/{} - User: {}", id, currentUser.getId());

        bookingService.deleteBooking(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(null, "Booking deleted successfully"));
    }

    /**
     * Get bookings for a venue (for vendor/admin)
     * GET /api/bookings/venue/{venueId}
     */
    @GetMapping("/venue/{venueId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getVenueBookings(
            @PathVariable Long venueId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/venue/{}", venueId);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookingResponse> bookings = bookingService.getVendorBookings(venueId, pageable, currentUser);

        return ResponseEntity.ok(ApiResponse.success(bookings, "Venue bookings retrieved successfully"));
    }

    /**
     * Get bookings by status for vendor
     * GET /api/bookings/vendor/{vendorId}/status/{status}
     */
    @GetMapping("/vendor/{vendorId}/status/{status}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getBookingsByStatus(
            @PathVariable Long vendorId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("GET /api/bookings/vendor/{}/status/{}", vendorId, status);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookingResponse> bookings = bookingService.getBookingsByStatus(vendorId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(bookings, "Bookings retrieved successfully"));
    }

    /**
     * Confirm booking (Vendor/Admin only)
     * POST /api/bookings/{id}/confirm
     */
    @PostMapping("/{id:\\d+}/confirm")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("POST /api/bookings/{}/confirm - User: {}", id, currentUser.getId());

        BookingResponse booking = bookingService.confirmBooking(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking confirmed successfully"));
    }

    /**
     * Complete booking (Vendor/Admin only)
     * POST /api/bookings/{id}/complete
     */
    @PostMapping("/{id:\\d+}/complete")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {

        log.info("POST /api/bookings/{}/complete - User: {}", id, currentUser.getId());

        BookingResponse booking = bookingService.completeBooking(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(booking, "Booking completed successfully"));
    }

    /**
     * Get vendor booking statistics
     * GET /api/bookings/vendor/statistics
     */
    @GetMapping("/vendor/statistics")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<VendorBookingStatsResponse>> getVendorBookingStatistics(
            @CurrentUser UserPrincipal currentUser) {

        log.info("GET /api/bookings/vendor/statistics - vendor: {}", currentUser.getId());

        VendorBookingStatsResponse stats = bookingService.getVendorBookingStats(currentUser.getId(), currentUser);

        return ResponseEntity.ok(ApiResponse.success(stats, "Vendor booking statistics retrieved successfully"));
    }

    /**
     * Get authenticated vendor bookings (or admin override via vendorId param)
     * GET /api/bookings/vendor
     */
    @GetMapping("/vendor")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAuthenticatedVendorBookings(
            @RequestParam(name = "vendorId", required = false) Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @CurrentUser UserPrincipal currentUser) {

        // Determine target vendor: admin can specify; vendor uses own id
        Long targetVendorId = (vendorId != null && isAdmin(currentUser)) ? vendorId : currentUser.getId();
        log.info("GET /api/bookings/vendor - targetVendorId: {} requestedBy: {}", targetVendorId, currentUser.getId());

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookingResponse> bookings = bookingService.getVendorBookings(targetVendorId, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success(bookings, "Vendor bookings retrieved successfully"));
    }

    /**
     * Reject booking (Vendor/Admin)
     * POST /api/bookings/{id}/reject
     */
    @PostMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBookingPost(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.rejectBooking(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking rejected successfully"));
    }

    /**
     * Reject booking (Vendor/Admin)
     * PUT /api/bookings/{id}/reject
     */
    @PutMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBookingPut(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.rejectBooking(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking rejected successfully"));
    }

    /**
     * Reject booking via vendor path variant to match UI expectations
     * POST /api/bookings/vendor/{id}/reject
     */
    @PostMapping("/vendor/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBookingVendorPath(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.rejectBooking(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking rejected successfully"));
    }

    /**
     * Reject booking via GET (to support legacy/UI behavior)
     * GET /api/bookings/{id}/reject
     */
    @GetMapping("/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBookingGet(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.rejectBooking(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking rejected successfully"));
    }

    /**
     * Reject booking via vendor path variant via GET
     * GET /api/bookings/vendor/{id}/reject
     */
    @GetMapping("/vendor/{id:\\d+}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBookingVendorPathGet(
            @PathVariable Long id,
            @RequestParam(name = "reason", required = false) String reason,
            @CurrentUser UserPrincipal currentUser) {
        BookingResponse booking = bookingService.rejectBooking(id, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(booking, "Booking rejected successfully"));
    }

    private boolean isAdmin(UserPrincipal user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}


package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.BookingRequest;
import com.myapp.booking.dtos.responses.BookingResponse;
import com.myapp.booking.dtos.responses.VendorBookingStatsResponse;
import com.myapp.booking.enums.TimeSlot;
import com.myapp.booking.exceptions.BadRequestException;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.exceptions.UnauthorizedException;
import com.myapp.booking.models.Booking;
import com.myapp.booking.models.Post;
import com.myapp.booking.repositories.BookingRepository;
import com.myapp.booking.repositories.PostRepository;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.security.UserPrincipal;
import com.myapp.booking.services.interfaces.IBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Create new booking - supports both legacy time-based and new slot-based requests
     */
    @Override
    public BookingResponse createBooking(BookingRequest request, UserPrincipal currentUser) {
        log.info("📥 Creating booking for user: {}", currentUser.getId());
        log.info("📝 Request details - postId: {}, slotIndex: {}, numberOfGuests: {}, specialRequests: '{}'",
                request.getPostId(), request.getSlotIndex(), request.getNumberOfGuests(), request.getSpecialRequests());
        log.info("📅 Booking time - startTime: {}, endTime: {}, bookingDateTime: {}",
                request.getStartTime(), request.getEndTime(), request.getBookingDateTime());

        // Validate required fields
        if (request.getPostId() == null) {
            throw new BadRequestException("Post ID is required");
        }

        // Validate venue (Post) exists and is active
        Post venue = postRepository.findByIdAndIsDeletedFalse(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        if (!venue.getIsActive()) {
            throw new BadRequestException("Venue is not active");
        }

        if (venue.getStatus() != Post.PostStatus.PUBLISHED) {
            throw new BadRequestException("Venue is not available for booking");
        }

        log.info("🏢 Venue info - id: {}, title: '{}', capacity: {}",
                venue.getId(), venue.getTitle(), venue.getCapacity());

        // Normalize booking date
        Date bookingDate = request.getBookingDate();
        if (bookingDate == null && request.getBookingDateTime() != null) {
            bookingDate = Date.valueOf(request.getBookingDateTime().toLocalDate());
        }
        if (bookingDate == null) {
            throw new BadRequestException("Booking date is required");
        }

        // Determine slot index and time information
        Integer slotIndex;
        TimeSlot timeSlot;
        Time startTime;
        Time endTime;

        if (request.getSlotIndex() != null) {
            // NEW API: Use provided slot index
            try {
                slotIndex = request.getSlotIndex();
                timeSlot = TimeSlot.fromIndex(slotIndex);
                startTime = Time.valueOf(timeSlot.getStartTime());
                endTime = Time.valueOf(timeSlot.getEndTime());
                log.info("Using new slot-based API with slot: {}", timeSlot.getDisplayText());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid slot index. Must be between 0 and 3 (0=10-12h, 1=12-14h, 2=14-16h, 3=16-18h)");
            }
        } else {
            // LEGACY API: Convert time strings to slot index for backward compatibility
            log.info("Using legacy time-based API, converting to slots");

            LocalTime requestedStartTime = parseTimeFromRequest(request);
            if (requestedStartTime == null) {
                throw new BadRequestException("Either 'slotIndex' (0-3) or 'startTime' (10:00, 12:00, 14:00, or 16:00) must be provided. " +
                        "Available slots: 0=10-12h, 1=12-14h, 2=14-16h, 3=16-18h");
            }

            // Find the matching time slot
            timeSlot = findMatchingTimeSlot(requestedStartTime);
            if (timeSlot == null) {
                throw new BadRequestException(String.format(
                        "Start time '%s' doesn't match available slots. Please use: 10:00 (Slot 0), 12:00 (Slot 1), 14:00 (Slot 2), or 16:00 (Slot 3)",
                        requestedStartTime));
            }

            slotIndex = timeSlot.getIndex();
            startTime = Time.valueOf(timeSlot.getStartTime());
            endTime = Time.valueOf(timeSlot.getEndTime());

            log.info("Converted legacy time {} to slot: {}", requestedStartTime, timeSlot.getDisplayText());
        }

        // Check if the selected slot is already booked
        boolean isSlotTaken = bookingRepository.existsByPostIdAndBookingDateAndSlotIndex(
                request.getPostId(), bookingDate, slotIndex);

        if (isSlotTaken) {
            throw new BadRequestException(String.format(
                    "Time slot %s is already booked for this date. Please choose a different slot.",
                    timeSlot.getDisplayText()));
        }

        // Unit price fallback to post.price if null
        Double unitPrice = request.getUnitPrice();
        if (unitPrice == null) {
            unitPrice = venue.getPrice() != null ? venue.getPrice().doubleValue() : 0.0;
        }
        if (unitPrice <= 0) {
            throw new BadRequestException("Unit price must be positive");
        }

        // Validate guest count does not exceed venue capacity
        if (request.getNumberOfGuests() != null && venue.getCapacity() != null
                && request.getNumberOfGuests() > venue.getCapacity()) {
            log.error("❌ Guest count validation failed - requested: {}, venue capacity: {}",
                    request.getNumberOfGuests(), venue.getCapacity());
            throw new BadRequestException(String.format(
                    "Guest count (%d) exceeds venue capacity (%d)",
                    request.getNumberOfGuests(), venue.getCapacity()));
        }

        // Calculate total amount - use actual guest count from request
        int guests = request.getNumberOfGuests() != null ? request.getNumberOfGuests() : 1;
        double totalAmount = unitPrice * guests;
        double discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : 0.0;
        double finalAmount = totalAmount - discount;

        log.info("💰 Price calculation - guests: {}, unitPrice: {}, totalAmount: {}, discount: {}, finalAmount: {}",
                guests, unitPrice, totalAmount, discount, finalAmount);

        // Create booking entity
        Booking booking = new Booking();
        booking.setUserId(currentUser.getId());
        booking.setPostId(request.getPostId());
        booking.setVenueId(request.getPostId()); // venue_id is the same as post_id
        booking.setVendorId(venue.getVendorId());
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setSlotIndex(slotIndex != null ? slotIndex : 0); // Ensure slotIndex is never null
        booking.setDurationHours(2.0); // Fixed 2-hour slots
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setUnitPrice(unitPrice);
        booking.setTotalAmount(totalAmount);
        booking.setDepositAmount(request.getDepositAmount());
        booking.setDiscountAmount(discount);
        booking.setFinalAmount(finalAmount);
        booking.setAdditionalServices(request.getAdditionalServices());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setStatus("PENDING");

        log.info("💾 About to save booking:");
        log.info("   - numberOfGuests: {}", booking.getNumberOfGuests());
        log.info("   - specialRequests: '{}'", booking.getSpecialRequests());
        log.info("   - totalAmount: {}, finalAmount: {}", booking.getTotalAmount(), booking.getFinalAmount());

        // Save booking
        Booking savedBooking = bookingRepository.save(booking);

        log.info("✅ Booking saved successfully with ID: {}", savedBooking.getId());
        log.info("   - Saved numberOfGuests: {}", savedBooking.getNumberOfGuests());
        log.info("   - Saved specialRequests: '{}'", savedBooking.getSpecialRequests());

        // Increment booking count in Post
        venue.incrementBookingCount();
        postRepository.save(venue);

        log.info("Booking created successfully: {} for slot {}",
                savedBooking.getId(), timeSlot.getDisplayText());

        return BookingResponse.fromEntity(savedBooking);
    }

    /**
     * Parse time from legacy request format
     */
    private LocalTime parseTimeFromRequest(BookingRequest request) {
        // Try startTime string first
        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            return parseTimeString(request.getStartTime().trim());
        }

        // Try bookingDateTime
        if (request.getBookingDateTime() != null) {
            return request.getBookingDateTime().toLocalTime();
        }

        return null;
    }

    /**
     * Parse time string flexibly
     */
    private LocalTime parseTimeString(String timeStr) {
        try {
            // Handle common formats: "10:00", "10", "10:00:00"
            if (timeStr.matches("\\d{1,2}")) {
                // Just hour: "10" -> "10:00"
                int hour = Integer.parseInt(timeStr);
                return LocalTime.of(hour, 0);
            } else if (timeStr.matches("\\d{1,2}:\\d{2}")) {
                // HH:mm format: "10:00"
                return LocalTime.parse(timeStr);
            } else if (timeStr.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                // HH:mm:ss format: "10:00:00"
                return LocalTime.parse(timeStr);
            }
        } catch (Exception e) {
            log.warn("Failed to parse time string: {}", timeStr, e);
        }
        return null;
    }

    /**
     * Find the time slot that matches the requested start time
     */
    private TimeSlot findMatchingTimeSlot(LocalTime requestedTime) {
        for (TimeSlot slot : TimeSlot.values()) {
            if (slot.getStartTime().equals(requestedTime)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Compute slot index from start time for existing bookings that don't have slotIndex set
     */
    private Integer computeSlotIndexFromTime(Time startTime) {
        if (startTime == null) {
            return 0; // Default to first slot
        }

        LocalTime localStartTime = startTime.toLocalTime();
        TimeSlot matchingSlot = findMatchingTimeSlot(localStartTime);

        if (matchingSlot != null) {
            return matchingSlot.getIndex();
        }

        // Fallback: find the closest slot based on time
        LocalTime time = startTime.toLocalTime();
        if (time.isBefore(LocalTime.of(12, 0))) {
            return 0; // 10-12 slot
        } else if (time.isBefore(LocalTime.of(14, 0))) {
            return 1; // 12-14 slot
        } else if (time.isBefore(LocalTime.of(16, 0))) {
            return 2; // 14-16 slot
        } else {
            return 3; // 16-18 slot
        }
    }

    /**
     * Get user's bookings
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        log.info("Fetching bookings for user: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Booking> bookings = bookingRepository.findByUserId(userId, pageable);

        return bookings.map(booking -> {
            BookingResponse response = BookingResponse.fromEntity(booking);

            // Enrich with venue details
            postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                    .ifPresent(venue -> {
                        response.setVenueName(venue.getTitle());
                        if (venue.getImages() != null && !venue.getImages().isEmpty()) {
                            response.setVenueImage(venue.getImages().get(0));
                        }
                        // Ensure venueId is set (fallback to postId if null)
                        if (response.getVenueId() == null) {
                            response.setVenueId(booking.getPostId());
                        }
                    });

            return response;
        });
    }

    /**
     * Get booking by ID
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, UserPrincipal currentUser) {
        log.info("Fetching booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Check authorization
        if (!booking.getUserId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        BookingResponse response = BookingResponse.fromEntity(booking);

        // Enrich with venue details
        postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                .ifPresent(venue -> {
                    response.setVenueName(venue.getTitle());
                    if (venue.getImages() != null && !venue.getImages().isEmpty()) {
                        response.setVenueImage(venue.getImages().get(0));
                    }
                    // Ensure venueId is set (for legacy bookings)
                    if (response.getVenueId() == null) {
                        response.setVenueId(booking.getPostId());
                    }
                });

        return response;
    }

    /**
     * Cancel booking
     */
    public BookingResponse cancelBooking(Long bookingId, UserPrincipal currentUser) {
        log.info("Cancelling booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Check authorization
        if (!booking.getUserId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        // Check if booking can be cancelled
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if ("COMPLETED".equals(booking.getStatus())) {
            throw new BadRequestException("Cannot cancel completed booking");
        }

        booking.setStatus("CANCELLED");
        booking.setCancelledBy(currentUser.getId());
        booking.setCancelledAt(LocalDateTime.now());
        Booking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking cancelled successfully: {}", bookingId);

        return BookingResponse.fromEntity(cancelledBooking);
    }

    /**
     * Get bookings by vendor (for vendor/admin)
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getVendorBookings(Long vendorId, Pageable pageable, UserPrincipal currentUser) {
        log.info("Fetching bookings for vendor: {}", vendorId);

        // Check authorization - only vendor can see their own bookings or admin can see all
        if (!vendorId.equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to view these bookings");
        }

        Page<Booking> bookings = bookingRepository.findByVendorId(vendorId, pageable);

        return bookings.map(booking -> {
            BookingResponse response = BookingResponse.fromEntity(booking);

            // Enrich with venue details
            postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                    .ifPresent(venue -> {
                        response.setVenueName(venue.getTitle());
                        if (venue.getImages() != null && !venue.getImages().isEmpty()) {
                            response.setVenueImage(venue.getImages().get(0));
                        }
                        // Ensure venueId is set (fallback to postId if null)
                        if (response.getVenueId() == null) {
                            response.setVenueId(booking.getPostId());
                        }
                    });

            return response;
        });
    }

    /**
     * Confirm booking (Vendor/Admin only)
     */
    public com.myapp.booking.dtos.responses.BookingConfirmResponse confirmBooking(Long bookingId, UserPrincipal currentUser) {
        log.info("Confirming booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Get venue to check ownership
        Post venue = postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        // Check authorization - only venue owner or admin
        if (!venue.getVendorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to confirm this booking");
        }

        // If already confirmed, return the booking (idempotent operation)
        if ("CONFIRMED".equals(booking.getStatus())) {
            log.info("Booking {} is already confirmed, returning existing booking", bookingId);

            // Get slot availability for the booking date
            com.myapp.booking.dtos.responses.SlotAvailabilityResponse slotAvailability =
                getSlotAvailability(booking.getPostId(), booking.getBookingDate());

            return com.myapp.booking.dtos.responses.BookingConfirmResponse.builder()
                    .booking(BookingResponse.fromEntity(booking))
                    .slotAvailability(slotAvailability)
                    .build();
        }

        // Only pending bookings can be confirmed
        if (!"PENDING".equals(booking.getStatus())) {
            throw new BadRequestException("Only pending bookings can be confirmed. Current status: " + booking.getStatus());
        }

        booking.setStatus("CONFIRMED");
        booking.setConfirmedBy(currentUser.getId());
        booking.setConfirmedAt(LocalDateTime.now());
        Booking confirmedBooking = bookingRepository.save(booking);

        log.info("Booking confirmed successfully: {}", bookingId);

        // Get updated slot availability for the booking date
        com.myapp.booking.dtos.responses.SlotAvailabilityResponse slotAvailability =
            getSlotAvailability(confirmedBooking.getPostId(), confirmedBooking.getBookingDate());

        // Return combined response with booking details and slot availability
        return com.myapp.booking.dtos.responses.BookingConfirmResponse.builder()
                .booking(BookingResponse.fromEntity(confirmedBooking))
                .slotAvailability(slotAvailability)
                .build();
    }

    /**
     * Complete booking (Vendor/Admin only)
     */
    public BookingResponse completeBooking(Long bookingId, UserPrincipal currentUser) {
        log.info("Completing booking: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Get venue to check ownership
        Post venue = postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        // Check authorization
        if (!venue.getVendorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to complete this booking");
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new BadRequestException("Only confirmed bookings can be completed");
        }

        booking.setStatus("COMPLETED");
        booking.setCompletedAt(LocalDateTime.now());
        Booking completedBooking = bookingRepository.save(booking);

        log.info("Booking completed successfully: {}", bookingId);

        return BookingResponse.fromEntity(completedBooking);
    }

    /**
     * Delete booking (soft delete)
     */
    @Override
    public void deleteBooking(Long id, UserPrincipal currentUser) {
        log.info("Deleting booking: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Check authorization - only user who created booking or admin can delete
        if (!booking.getUserId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to delete this booking");
        }

        // Check if booking can be deleted
        if ("COMPLETED".equals(booking.getStatus())) {
            throw new BadRequestException("Cannot delete completed booking");
        }

        // Soft delete
        booking.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        log.info("Booking deleted successfully: {}", id);
    }

    /**
     * Get bookings by status for a vendor
     */
    @Override
    public Page<BookingResponse> getBookingsByStatus(Long vendorId, String status, Pageable pageable) {
        log.info("Fetching bookings for vendor: {} with status: {}", vendorId, status);

        Page<Booking> bookings = bookingRepository.findByVendorIdAndStatus(vendorId, status, pageable);

        return bookings.map(booking -> {
            BookingResponse response = BookingResponse.fromEntity(booking);

            // Enrich with venue details
            postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                    .ifPresent(venue -> {
                        response.setVenueName(venue.getTitle());
                        if (venue.getImages() != null && !venue.getImages().isEmpty()) {
                            response.setVenueImage(venue.getImages().get(0));
                        }
                        // Ensure venueId is set (fallback to postId if null)
                        if (response.getVenueId() == null) {
                            response.setVenueId(booking.getPostId());
                        }
                    });

            return response;
        });
    }

    /**
     * Check if a time slot is available for a venue
     * If requestedDate time is midnight (00:00), it checks general availability for the whole day
     * Otherwise, it validates the specific time is within working hours (10:00-18:00)
     */
    @Override
    public boolean isTimeSlotAvailable(Long venueId, LocalDateTime requestedDate) {
        log.info("Checking time slot availability for venue: {} on date: {}", venueId, requestedDate);

        // Get the post to check available slots
        Post venue = postRepository.findByIdAndIsDeletedFalse(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        // Convert LocalDateTime to java.sql.Date for database query
        java.sql.Date bookingDate = java.sql.Date.valueOf(requestedDate.toLocalDate());
        LocalTime requestedTime = requestedDate.toLocalTime();

        // Only validate time if it's a specific time request (not midnight/start of day)
        // Midnight (00:00) indicates checking general availability for the whole day
        if (requestedTime.getHour() != 0 || requestedTime.getMinute() != 0) {
            // Validate time is within 10:00-18:00
            LocalTime startWorkingHour = LocalTime.of(10, 0);
            LocalTime endWorkingHour = LocalTime.of(18, 0);

            if (requestedTime.isBefore(startWorkingHour) || requestedTime.isAfter(endWorkingHour)) {
                log.warn("Requested time {} is outside working hours (10:00-18:00)", requestedTime);
                return false;
            }
        } else {
            log.debug("Checking general availability for the whole day (time: {})", requestedTime);
        }

        // Get total available slots for this post
        Integer totalSlots = venue.getAvailableSlots() != null ? venue.getAvailableSlots() : 4;

        // Count active bookings on this date (excluding CANCELLED)
        long bookedCount = bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                venueId,
                bookingDate,
                java.util.List.of("CANCELLED")
        );

        log.info("Venue {} on {}: Total slots = {}, Booked = {}", venueId, bookingDate, totalSlots, bookedCount);

        // Check if there are available slots
        return bookedCount < totalSlots;
    }

    /**
     * Convenience alias for controller compatibility
     */
    @Override
    public boolean checkAvailability(Long venueId, LocalDateTime requestedDate) {
        return isTimeSlotAvailable(venueId, requestedDate);
    }

    /**
     * Helper method to check if user is admin
     */
    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Get vendor booking statistics
     */
    public VendorBookingStatsResponse getVendorBookingStats(Long vendorId, UserPrincipal currentUser) {
        // Authorization: vendor requesting own stats or admin
        if (!vendorId.equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to view these statistics");
        }
        long total = bookingRepository.countByVendorId(vendorId);
        long pending = bookingRepository.countByVendorIdAndStatus(vendorId, "PENDING");
        long confirmed = bookingRepository.countByVendorIdAndStatus(vendorId, "CONFIRMED");
        long cancelled = bookingRepository.countByVendorIdAndStatus(vendorId, "CANCELLED");
        long completed = bookingRepository.countByVendorIdAndStatus(vendorId, "COMPLETED");
        long upcoming = bookingRepository.countUpcomingByVendor(vendorId);
        long today = bookingRepository.countTodayByVendor(vendorId);
        double revenue = bookingRepository.sumRevenueByVendor(vendorId);
        return VendorBookingStatsResponse.builder()
                .totalBookings(total)
                .pendingCount(pending)
                .confirmedCount(confirmed)
                .cancelledCount(cancelled)
                .completedCount(completed)
                .upcomingCount(upcoming)
                .todayCount(today)
                .totalRevenue(revenue)
                .build();
    }

    /**
     * Reject booking (Vendor/Admin only) - sets status to CANCELLED and records reason
     */
    public BookingResponse rejectBooking(Long bookingId, String reason, UserPrincipal currentUser) {
        log.info("Rejecting booking: {} by user: {}", bookingId, currentUser.getId());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Get venue to check ownership
        Post venue = postRepository.findByIdAndIsDeletedFalse(booking.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        // Authorization: only vendor owner or admin can reject
        if (!venue.getVendorId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to reject this booking");
        }

        // Only pending or confirmed bookings can be rejected
        if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
            throw new BadRequestException("Only pending or confirmed bookings can be rejected");
        }

        booking.setStatus("CANCELLED");
        booking.setCancelledBy(currentUser.getId());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);

        Booking rejected = bookingRepository.save(booking);
        return BookingResponse.fromEntity(rejected);
    }

    /**
     * Get detailed slot availability for a specific venue and date
     */
    public com.myapp.booking.dtos.responses.SlotAvailabilityResponse getSlotAvailability(
            Long postId,
            java.sql.Date bookingDate) {

        log.info("Getting slot availability for post: {} on date: {}", postId, bookingDate);

        // Get the post
        Post venue = postRepository.findByIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        // Get total available slots for this post (default: 4)
        Integer totalSlots = venue.getAvailableSlots() != null ? venue.getAvailableSlots() : 4;

        // Get existing bookings for this date to determine which slots are taken
        List<Booking> existingBookings = bookingRepository.findByPostIdAndBookingDateAndStatusNotIn(
                postId,
                bookingDate,
                java.util.List.of("CANCELLED")
        );

        // Build slot info for all 4 time slots (0-3)
        java.util.List<com.myapp.booking.dtos.responses.SlotAvailabilityResponse.SlotInfo> slots = new java.util.ArrayList<>();

        for (com.myapp.booking.enums.TimeSlot timeSlot : com.myapp.booking.enums.TimeSlot.values()) {
            // Check if this slot is already booked
            boolean isBooked = existingBookings.stream()
                    .anyMatch(b -> b.getSlotIndex() != null && b.getSlotIndex().equals(timeSlot.getIndex()));

            slots.add(com.myapp.booking.dtos.responses.SlotAvailabilityResponse.SlotInfo.builder()
                    .slotIndex(timeSlot.getIndex())
                    .startTime(timeSlot.getStartTime())
                    .endTime(timeSlot.getEndTime())
                    .displayText(timeSlot.getDisplayText())
                    .isAvailable(!isBooked)
                    .status(isBooked ? "BOOKED" : "AVAILABLE")
                    .build());
        }

        // Count how many slots are actually booked
        long bookedCount = existingBookings.size();
        Integer availableSlots = totalSlots - (int) bookedCount;

        return com.myapp.booking.dtos.responses.SlotAvailabilityResponse.builder()
                .postId(postId)
                .postTitle(venue.getTitle())
                .bookingDate(bookingDate.toString())
                .totalSlots(totalSlots)
                .availableSlots(availableSlots >= 0 ? availableSlots : 0)
                .bookedSlots((int) bookedCount)
                .slots(slots)
                .build();
    }
}

package com.myapp.booking.services;

import com.myapp.booking.models.Post;
import com.myapp.booking.repositories.BookingRepository;
import com.myapp.booking.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for availability checking with different time scenarios
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceAvailabilityTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private BookingService bookingService;

    /**
     * Test whole-day availability check (midnight time)
     * This is what happens when controller calls with atStartOfDay()
     */
    @Test
    void testCheckAvailability_WholeDayCheck_ShouldNotValidateTime() {
        // Given
        Long postId = 8L;
        LocalDate date = LocalDate.of(2025, 11, 20);
        LocalDateTime midnightDateTime = date.atStartOfDay(); // 2025-11-20T00:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(2L); // 2 bookings out of 4 slots

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, midnightDateTime);

        // Then
        assertTrue(isAvailable, "Should be available when 2 out of 4 slots are booked");

        // Verify time validation was skipped (no exception thrown for midnight time)
        verify(bookingRepository).countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        );
    }

    /**
     * Test specific time within working hours
     */
    @Test
    void testCheckAvailability_SpecificTimeInWorkingHours_ShouldValidateAndCheckSlots() {
        // Given
        Long postId = 8L;
        LocalDateTime afternoonTime = LocalDateTime.of(2025, 11, 20, 14, 0); // 14:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(1L); // 1 booking out of 4 slots

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, afternoonTime);

        // Then
        assertTrue(isAvailable, "Should be available when 1 out of 4 slots is booked");
    }

    /**
     * Test specific time outside working hours (before 10:00)
     */
    @Test
    void testCheckAvailability_TimeBeforeWorkingHours_ShouldReturnFalse() {
        // Given
        Long postId = 8L;
        LocalDateTime earlyMorning = LocalDateTime.of(2025, 11, 20, 8, 0); // 08:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, earlyMorning);

        // Then
        assertFalse(isAvailable, "Should NOT be available for time before working hours");

        // Verify we didn't even check the database (rejected by time validation)
        verify(bookingRepository, never()).countByPostIdAndBookingDateAndStatusNotIn(
                anyLong(),
                any(java.sql.Date.class),
                anyList()
        );
    }

    /**
     * Test specific time outside working hours (after 18:00)
     */
    @Test
    void testCheckAvailability_TimeAfterWorkingHours_ShouldReturnFalse() {
        // Given
        Long postId = 8L;
        LocalDateTime evening = LocalDateTime.of(2025, 11, 20, 19, 0); // 19:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, evening);

        // Then
        assertFalse(isAvailable, "Should NOT be available for time after working hours");

        // Verify we didn't even check the database
        verify(bookingRepository, never()).countByPostIdAndBookingDateAndStatusNotIn(
                anyLong(),
                any(java.sql.Date.class),
                anyList()
        );
    }

    /**
     * Test fully booked venue (whole day check)
     */
    @Test
    void testCheckAvailability_FullyBooked_ShouldReturnFalse() {
        // Given
        Long postId = 8L;
        LocalDateTime midnightDateTime = LocalDate.of(2025, 11, 20).atStartOfDay();

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(4L); // 4 out of 4 slots booked (fully booked)

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, midnightDateTime);

        // Then
        assertFalse(isAvailable, "Should NOT be available when fully booked");
    }

    /**
     * Test edge case: exactly at start of working hours (10:00)
     */
    @Test
    void testCheckAvailability_ExactlyAtWorkingHourStart_ShouldBeValid() {
        // Given
        Long postId = 8L;
        LocalDateTime tenAM = LocalDateTime.of(2025, 11, 20, 10, 0); // 10:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(0L);

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, tenAM);

        // Then
        assertTrue(isAvailable, "Should be available at exactly 10:00 (start of working hours)");
    }

    /**
     * Test edge case: exactly at end of working hours (18:00)
     */
    @Test
    void testCheckAvailability_ExactlyAtWorkingHourEnd_ShouldBeValid() {
        // Given
        Long postId = 8L;
        LocalDateTime sixPM = LocalDateTime.of(2025, 11, 20, 18, 0); // 18:00

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(4)
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(0L);

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, sixPM);

        // Then
        assertTrue(isAvailable, "Should be available at exactly 18:00 (end of working hours)");
    }

    /**
     * Test with null availableSlots (should default to 4)
     */
    @Test
    void testCheckAvailability_NullAvailableSlots_ShouldDefaultToFour() {
        // Given
        Long postId = 8L;
        LocalDateTime midnightDateTime = LocalDate.of(2025, 11, 20).atStartOfDay();

        Post mockPost = Post.builder()
                .id(postId)
                .title("Test Venue")
                .availableSlots(null) // NULL should default to 4
                .build();

        when(postRepository.findByIdAndIsDeletedFalse(postId))
                .thenReturn(Optional.of(mockPost));

        when(bookingRepository.countByPostIdAndBookingDateAndStatusNotIn(
                eq(postId),
                any(java.sql.Date.class),
                anyList()
        )).thenReturn(3L); // 3 out of default 4 slots

        // When
        boolean isAvailable = bookingService.isTimeSlotAvailable(postId, midnightDateTime);

        // Then
        assertTrue(isAvailable, "Should be available when 3 out of 4 (default) slots are booked");
    }
}


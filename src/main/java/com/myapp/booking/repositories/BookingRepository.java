package com.myapp.booking.repositories;

import com.myapp.booking.models.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings by user ID
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    // Find bookings by user ID and status
    Page<Booking> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    // Find all bookings by post ID
    Page<Booking> findByPostId(Long postId, Pageable pageable);

    // Find bookings by post ID and status
    Page<Booking> findByPostIdAndStatus(Long postId, String status, Pageable pageable);

    // Find all bookings by vendor ID
    Page<Booking> findByVendorId(Long vendorId, Pageable pageable);

    // Find bookings by vendor ID and status
    Page<Booking> findByVendorIdAndStatus(Long vendorId, String status, Pageable pageable);

    // Find booking by ID and user ID (for authorization)
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    // Find bookings by user ID and date range
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
            "AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") java.sql.Date startDate,
            @Param("endDate") java.sql.Date endDate
    );

    // Check if venue is already booked at specific time
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.postId = :postId " +
            "AND b.bookingDate = :bookingDate AND b.status NOT IN ('CANCELLED') AND b.deletedAt IS NULL")
    boolean existsByPostIdAndBookingDateAndStatusNot(
            @Param("postId") Long postId,
            @Param("bookingDate") java.sql.Date bookingDate
    );

    // Count bookings for a post on a specific date excluding certain statuses
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.postId = :postId " +
            "AND b.bookingDate = :bookingDate AND b.status NOT IN :excludedStatuses AND b.deletedAt IS NULL")
    long countByPostIdAndBookingDateAndStatusNotIn(
            @Param("postId") Long postId,
            @Param("bookingDate") java.sql.Date bookingDate,
            @Param("excludedStatuses") List<String> excludedStatuses
    );

    // Find bookings for a post on a specific date excluding certain statuses
    @Query("SELECT b FROM Booking b WHERE b.postId = :postId " +
            "AND b.bookingDate = :bookingDate AND b.status NOT IN :excludedStatuses AND b.deletedAt IS NULL")
    List<Booking> findByPostIdAndBookingDateAndStatusNotIn(
            @Param("postId") Long postId,
            @Param("bookingDate") java.sql.Date bookingDate,
            @Param("excludedStatuses") List<String> excludedStatuses
    );

    // Count bookings by status for a user
    long countByUserIdAndStatus(Long userId, String status);

    // Count total bookings for a venue (post)
    long countByPostId(Long postId);

    // Count total bookings for a vendor
    long countByVendorId(Long vendorId);

    // Count bookings for a vendor by status
    long countByVendorIdAndStatus(Long vendorId, String status);

    // Calculate total revenue for a vendor from confirmed and completed bookings
    @Query("SELECT COALESCE(SUM(b.finalAmount),0) FROM Booking b WHERE b.vendorId = :vendorId AND b.status IN ('CONFIRMED','COMPLETED') AND b.deletedAt IS NULL")
    double sumRevenueByVendor(@Param("vendorId") Long vendorId);

    // Count upcoming bookings for a vendor
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vendorId = :vendorId AND b.bookingDate > CURRENT_DATE AND b.status IN ('PENDING','CONFIRMED') AND b.deletedAt IS NULL")
    long countUpcomingByVendor(@Param("vendorId") Long vendorId);

    // Count today's bookings for a vendor
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vendorId = :vendorId AND b.bookingDate = CURRENT_DATE AND b.status IN ('PENDING','CONFIRMED') AND b.deletedAt IS NULL")
    long countTodayByVendor(@Param("vendorId") Long vendorId);

    // Check if a specific slot is already booked for a post on a given date
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.postId = :postId " +
            "AND b.bookingDate = :bookingDate AND b.slotIndex = :slotIndex " +
            "AND b.status NOT IN ('CANCELLED') AND b.deletedAt IS NULL")
    boolean existsByPostIdAndBookingDateAndSlotIndex(
            @Param("postId") Long postId,
            @Param("bookingDate") java.sql.Date bookingDate,
            @Param("slotIndex") Integer slotIndex
    );

    // Find bookings by slot index for a post on a specific date
    @Query("SELECT b FROM Booking b WHERE b.postId = :postId " +
            "AND b.bookingDate = :bookingDate AND b.slotIndex = :slotIndex " +
            "AND b.status NOT IN ('CANCELLED') AND b.deletedAt IS NULL")
    List<Booking> findByPostIdAndBookingDateAndSlotIndex(
            @Param("postId") Long postId,
            @Param("bookingDate") java.sql.Date bookingDate,
            @Param("slotIndex") Integer slotIndex
    );
}
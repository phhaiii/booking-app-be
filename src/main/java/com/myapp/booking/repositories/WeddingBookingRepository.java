package com.myapp.booking.repositories;

import com.myapp.booking.models.WeddingBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeddingBookingRepository extends JpaRepository<WeddingBooking, Long> {

    // Find by vendor ID and status
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.status = :status AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findByVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") WeddingBooking.BookingStatus status
    );

    // Find all by vendor ID
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findByVendorIdAndNotDeleted(@Param("vendorId") Long vendorId);

    // Find by ID and vendor ID
    @Query("SELECT b FROM WeddingBooking b WHERE b.id = :id AND b.vendorId = :vendorId AND b.deletedAt IS NULL")
    Optional<WeddingBooking> findByIdAndVendorId(
            @Param("id") Long id,
            @Param("vendorId") Long vendorId
    );

    // Find by user ID
    @Query("SELECT b FROM WeddingBooking b WHERE b.userId = :userId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findByUserId(@Param("userId") Long userId);

    // Find by venue ID
    @Query("SELECT b FROM WeddingBooking b WHERE b.venueId = :venueId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findByVenueId(@Param("venueId") Long venueId);

    // Find by date range
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.requestedDate BETWEEN :startDate AND :endDate AND b.deletedAt IS NULL ORDER BY b.requestedDate ASC")
    List<WeddingBooking> findByVendorIdAndDateRange(
            @Param("vendorId") Long vendorId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find overdue bookings
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.status = 'PENDING' AND b.requestedDate < :now AND b.deletedAt IS NULL ORDER BY b.requestedDate ASC")
    List<WeddingBooking> findOverdueByVendorId(
            @Param("vendorId") Long vendorId,
            @Param("now") LocalDateTime now
    );

    // Count by status
    @Query("SELECT COUNT(b) FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.status = :status AND b.deletedAt IS NULL")
    Long countByVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") WeddingBooking.BookingStatus status
    );

    // Count total
    @Query("SELECT COUNT(b) FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.deletedAt IS NULL")
    Long countByVendorId(@Param("vendorId") Long vendorId);

    // Find by service type
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.serviceType = :serviceType AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findByVendorIdAndServiceType(
            @Param("vendorId") Long vendorId,
            @Param("serviceType") WeddingBooking.ServiceType serviceType
    );

    // Search bookings
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND " +
            "(LOWER(b.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.serviceName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> searchByKeyword(
            @Param("vendorId") Long vendorId,
            @Param("keyword") String keyword
    );

    // Get statistics
    @Query("SELECT SUM(b.budget) FROM WeddingBooking b WHERE b.vendorId = :vendorId AND b.status = :status AND b.deletedAt IS NULL")
    Double sumBudgetByVendorIdAndStatus(
            @Param("vendorId") Long vendorId,
            @Param("status") WeddingBooking.BookingStatus status
    );

    // Find bookings created today
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND DATE(b.createdAt) = CURRENT_DATE AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findTodayBookings(@Param("vendorId") Long vendorId);

    // Find bookings for this week
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND WEEK(b.createdAt) = WEEK(CURRENT_DATE) AND YEAR(b.createdAt) = YEAR(CURRENT_DATE) AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findThisWeekBookings(@Param("vendorId") Long vendorId);

    // Find bookings for this month
    @Query("SELECT b FROM WeddingBooking b WHERE b.vendorId = :vendorId AND MONTH(b.createdAt) = MONTH(CURRENT_DATE) AND YEAR(b.createdAt) = YEAR(CURRENT_DATE) AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<WeddingBooking> findThisMonthBookings(@Param("vendorId") Long vendorId);

    // Check if time slot is available
    @Query("SELECT COUNT(b) FROM WeddingBooking b WHERE b.venueId = :venueId AND b.requestedDate = :requestedDate AND b.status IN ('PENDING', 'CONFIRMED') AND b.deletedAt IS NULL")
    Long countConflictingBookings(
            @Param("venueId") Long venueId,
            @Param("requestedDate") LocalDateTime requestedDate
    );
}
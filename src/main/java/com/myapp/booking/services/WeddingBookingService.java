package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.booking.BookingActionRequest;
import com.myapp.booking.dtos.requests.booking.BookingRequest;
import com.myapp.booking.dtos.requests.booking.BookingUpdateRequest;
import com.myapp.booking.dtos.responses.booking.BookingResponse;
import com.myapp.booking.dtos.responses.booking.BookingStatisticsResponse;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.exceptions.BadRequestException;
import com.myapp.booking.models.WeddingBooking;
import com.myapp.booking.repositories.WeddingBookingRepository;
import com.myapp.booking.services.interfaces.IWeddingBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeddingBookingService implements IWeddingBookingService {

    private final WeddingBookingRepository repository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        log.info("Creating new booking for user: {}", userId);

        // Validate time slot availability
        if (request.getVenueId() != null && request.getRequestedDate() != null) {
            if (!isTimeSlotAvailable(request.getVenueId(), request.getRequestedDate())) {
                throw new BadRequestException("Khung giờ này đã được đặt. Vui lòng chọn thời gian khác.");
            }
        }

        WeddingBooking booking = WeddingBooking.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .venueId(request.getVenueId())
                .serviceName(request.getServiceName())
                .serviceType(request.getServiceType())
                .requestedDate(request.getRequestedDate())
                .numberOfGuests(request.getNumberOfGuests())
                .budget(request.getBudget())
                .message(request.getMessage())
                .vendorId(request.getVendorId())
                .userId(userId)
                .status(WeddingBooking.BookingStatus.PENDING)
                .build();

        WeddingBooking saved = repository.save(booking);
        log.info("Created booking with id: {}", saved.getId());

        return BookingResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public BookingResponse updateBooking(Long id, BookingUpdateRequest request, Long vendorId) {
        log.info("Updating booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        // Update fields if provided
        if (request.getCustomerName() != null) {
            booking.setCustomerName(request.getCustomerName());
        }
        if (request.getCustomerPhone() != null) {
            booking.setCustomerPhone(request.getCustomerPhone());
        }
        if (request.getCustomerEmail() != null) {
            booking.setCustomerEmail(request.getCustomerEmail());
        }
        if (request.getServiceType() != null) {
            booking.setServiceType(request.getServiceType());
        }
        if (request.getRequestedDate() != null) {
            booking.setRequestedDate(request.getRequestedDate());
        }
        if (request.getNumberOfGuests() != null) {
            booking.setNumberOfGuests(request.getNumberOfGuests());
        }
        if (request.getBudget() != null) {
            booking.setBudget(request.getBudget());
        }
        if (request.getMessage() != null) {
            booking.setMessage(request.getMessage());
        }

        WeddingBooking updated = repository.save(booking);
        log.info("Updated booking: {}", id);

        return BookingResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, Long vendorId) {
        log.info("Getting booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        return BookingResponse.fromEntity(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookingsByVendor(Long vendorId) {
        log.info("Getting all bookings for vendor: {}", vendorId);

        return repository.findByVendorIdAndNotDeleted(vendorId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBooking(Long id, Long vendorId) {
        log.info("Deleting booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        booking.setDeletedAt(LocalDateTime.now());
        repository.save(booking);

        log.info("Soft deleted booking: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStatus(Long vendorId, WeddingBooking.BookingStatus status) {
        log.info("Getting bookings with status: {} for vendor: {}", status, vendorId);

        return repository.findByVendorIdAndStatus(vendorId, status).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long id, Long vendorId, BookingActionRequest request) {
        log.info("Confirming booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() != WeddingBooking.BookingStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể xác nhận đặt lịch đang chờ xử lý");
        }

        booking.confirm();
        WeddingBooking updated = repository.save(booking);

        log.info("Confirmed booking: {}", id);
        return BookingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public BookingResponse rejectBooking(Long id, Long vendorId, BookingActionRequest request) {
        log.info("Rejecting booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() != WeddingBooking.BookingStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể từ chối đặt lịch đang chờ xử lý");
        }

        String reason = request != null ? request.getReason() : null;
        booking.reject(reason);
        WeddingBooking updated = repository.save(booking);

        log.info("Rejected booking: {}", id);
        return BookingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public BookingResponse completeBooking(Long id, Long vendorId) {
        log.info("Completing booking: {} for vendor: {}", id, vendorId);

        WeddingBooking booking = repository.findByIdAndVendorId(id, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (booking.getStatus() != WeddingBooking.BookingStatus.CONFIRMED) {
            throw new BadRequestException("Chỉ có thể hoàn thành đặt lịch đã xác nhận");
        }

        booking.complete();
        WeddingBooking updated = repository.save(booking);

        log.info("Completed booking: {}", id);
        return BookingResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, Long userId) {
        log.info("Cancelling booking: {} for user: {}", id, userId);

        WeddingBooking booking = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền hủy đặt lịch này");
        }

        if (booking.getStatus() == WeddingBooking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy đặt lịch đã hoàn thành");
        }

        booking.cancel();
        WeddingBooking updated = repository.save(booking);

        log.info("Cancelled booking: {}", id);
        return BookingResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByDateRange(Long vendorId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting bookings for vendor: {} between {} and {}", vendorId, startDate, endDate);

        return repository.findByVendorIdAndDateRange(vendorId, startDate, endDate).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByServiceType(Long vendorId, WeddingBooking.ServiceType serviceType) {
        log.info("Getting bookings with service type: {} for vendor: {}", serviceType, vendorId);

        return repository.findByVendorIdAndServiceType(vendorId, serviceType).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> searchBookings(Long vendorId, String keyword) {
        log.info("Searching bookings with keyword: {} for vendor: {}", keyword, vendorId);

        return repository.searchByKeyword(vendorId, keyword).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getOverdueBookings(Long vendorId) {
        log.info("Getting overdue bookings for vendor: {}", vendorId);

        return repository.findOverdueByVendorId(vendorId, LocalDateTime.now()).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        log.info("Getting bookings for user: {}", userId);

        return repository.findByUserId(userId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByVenue(Long venueId) {
        log.info("Getting bookings for venue: {}", venueId);

        return repository.findByVenueId(venueId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingStatisticsResponse getStatistics(Long vendorId) {
        log.info("Getting booking statistics for vendor: {}", vendorId);

        Long total = repository.countByVendorId(vendorId);
        Long pending = repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.PENDING);
        Long confirmed = repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.CONFIRMED);
        Long rejected = repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.REJECTED);
        Long completed = repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.COMPLETED);
        Long cancelled = repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.CANCELLED);
        Long overdue = (long) getOverdueBookings(vendorId).size();

        Double totalRevenue = repository.sumBudgetByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.COMPLETED);
        Double pendingRevenue = repository.sumBudgetByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.PENDING);
        Double confirmedRevenue = repository.sumBudgetByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.CONFIRMED);

        Long venueBookings = (long) repository.findByVendorIdAndServiceType(vendorId, WeddingBooking.ServiceType.VENUE).size();
        Long photographyBookings = (long) repository.findByVendorIdAndServiceType(vendorId, WeddingBooking.ServiceType.PHOTOGRAPHY).size();
        Long cateringBookings = (long) repository.findByVendorIdAndServiceType(vendorId, WeddingBooking.ServiceType.CATERING).size();
        Long decorationBookings = (long) repository.findByVendorIdAndServiceType(vendorId, WeddingBooking.ServiceType.DECORATION).size();
        Long fashionBookings = (long) repository.findByVendorIdAndServiceType(vendorId, WeddingBooking.ServiceType.FASHION).size();

        Long today = (long) getTodayBookings(vendorId).size();
        Long thisWeek = (long) getThisWeekBookings(vendorId).size();
        Long thisMonth = (long) getThisMonthBookings(vendorId).size();

        return BookingStatisticsResponse.builder()
                .totalBookings(total)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .rejectedBookings(rejected)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .overdueBookings(overdue)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .pendingRevenue(pendingRevenue != null ? pendingRevenue : 0.0)
                .confirmedRevenue(confirmedRevenue != null ? confirmedRevenue : 0.0)
                .venueBookings(venueBookings)
                .photographyBookings(photographyBookings)
                .cateringBookings(cateringBookings)
                .decorationBookings(decorationBookings)
                .fashionBookings(fashionBookings)
                .todayBookings(today)
                .thisWeekBookings(thisWeek)
                .thisMonthBookings(thisMonth)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPendingCount(Long vendorId) {
        return repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getConfirmedCount(Long vendorId) {
        return repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.CONFIRMED);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRejectedCount(Long vendorId) {
        return repository.countByVendorIdAndStatus(vendorId, WeddingBooking.BookingStatus.REJECTED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getTodayBookings(Long vendorId) {
        return repository.findTodayBookings(vendorId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getThisWeekBookings(Long vendorId) {
        return repository.findThisWeekBookings(vendorId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getThisMonthBookings(Long vendorId) {
        return repository.findThisMonthBookings(vendorId).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(Long venueId, LocalDateTime requestedDate) {
        Long conflictCount = repository.countConflictingBookings(venueId, requestedDate);
        return conflictCount == 0;
    }
}
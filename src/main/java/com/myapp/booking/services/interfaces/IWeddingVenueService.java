package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.WeddingVenueRequest;
import com.myapp.booking.dtos.responses.WeddingVenueResponse;

import java.util.List;
import java.util.Optional;

public interface IWeddingVenueService {

    /**
     * Tạo mới một địa điểm cưới
     */
    WeddingVenueResponse createVenue(WeddingVenueRequest request);

    /**
     * Cập nhật thông tin địa điểm cưới
     */
    WeddingVenueResponse updateVenue(Long id, WeddingVenueRequest request);

    /**
     * Lấy danh sách tất cả địa điểm cưới (chưa bị xóa)
     */
    List<WeddingVenueResponse> getAllVenues();

    /**
     * Lấy danh sách địa điểm cưới theo vendor
     */
    List<WeddingVenueResponse> getVenuesByVendor(Long vendorId);

    /**
     * Tìm kiếm địa điểm theo tên (không phân biệt hoa thường)
     */
    List<WeddingVenueResponse> searchVenuesByName(String name);

    /**
     * Lấy chi tiết địa điểm theo ID
     */
    Optional<WeddingVenueResponse> getVenueById(Long id);

    /**
     * Lấy chi tiết địa điểm theo slug
     */
    Optional<WeddingVenueResponse> getVenueBySlug(String slug);

    /**
     * Xóa mềm địa điểm cưới
     */
    void deleteVenue(Long id);

    /**
     * Cập nhật trạng thái khả dụng
     */
    void updateAvailability(Long id, Boolean isAvailable);
}
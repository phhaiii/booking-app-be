package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.WeddingVenueRequest;
import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.responses.WeddingVenueResponse;
import com.myapp.booking.services.interfaces.IWeddingVenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wedding-venues")
@RequiredArgsConstructor
public class WeddingVenueController {

    private final IWeddingVenueService weddingVenueService;

    /**
     * 🏠 Lấy danh sách tất cả địa điểm cưới (chưa bị xóa, đang khả dụng)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WeddingVenueResponse>>> getAllVenues() {
        List<WeddingVenueResponse> venues = weddingVenueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.success(venues,"Lấy danh sách địa điểm cưới thành công"));
    }

    /**
     * 🔍 Tìm kiếm địa điểm cưới theo tên (không phân biệt hoa thường)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<WeddingVenueResponse>>> searchVenuesByName(
            @RequestParam String name
    ) {
        List<WeddingVenueResponse> venues = weddingVenueService.searchVenuesByName(name);
        return ResponseEntity.ok(ApiResponse.success(venues,"Tìm kiếm địa điểm cưới thành công"));
    }

    /**
     * 👤 Lấy danh sách địa điểm cưới theo vendor
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<WeddingVenueResponse>>> getVenuesByVendor(
            @PathVariable Long vendorId
    ) {
        List<WeddingVenueResponse> venues = weddingVenueService.getVenuesByVendor(vendorId);
        return ResponseEntity.ok(ApiResponse.success(venues,"Lấy danh sách địa điểm cưới theo vendor thành công"));
    }

    /**
     * 📄 Lấy chi tiết địa điểm cưới theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WeddingVenueResponse>> getVenueById(
            @PathVariable Long id
    ) throws BadRequestException {
        return weddingVenueService.getVenueById(id)
                .map(venue -> ResponseEntity.ok(ApiResponse.success(venue, "Lấy chi tiết địa điểm cưới thành công")))
                .orElseThrow(() -> new BadRequestException("Không tìm thấy địa điểm cưới với ID: " + id));
    }

    /**
     * 🔗 Lấy chi tiết địa điểm cưới theo slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<WeddingVenueResponse>> getVenueBySlug(
            @PathVariable String slug
    ) throws BadRequestException {
        return weddingVenueService.getVenueBySlug(slug)
                .map(venue -> ResponseEntity.ok(ApiResponse.success(venue, "Lấy chi tiết địa điểm cưới thành công")))
                .orElseThrow(() -> new BadRequestException("Không tìm thấy địa điểm cưới với slug: " + slug));
    }

    /**
     * ➕ Tạo mới một địa điểm cưới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeddingVenueResponse>> createVenue(
            @Valid @RequestBody WeddingVenueRequest request
    ) {
        WeddingVenueResponse response = weddingVenueService.createVenue(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tạo địa điểm cưới thành công"));
    }

    /**
     * ✏️ Cập nhật thông tin địa điểm cưới
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WeddingVenueResponse>> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody WeddingVenueRequest request
    ) throws BadRequestException {
        WeddingVenueResponse response = weddingVenueService.updateVenue(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật địa điểm cưới thành công"));
    }

    /**
     * 🔄 Cập nhật trạng thái khả dụng (isAvailable)
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @PathVariable Long id,
            @RequestParam Boolean isAvailable
    ) {
        weddingVenueService.updateAvailability(id, isAvailable);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái địa điểm cưới thành công"));
    }

    /**
     * 🗑️ Xóa mềm địa điểm cưới
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(
            @PathVariable Long id
    ) {
        weddingVenueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa địa điểm cưới thành công"));
    }

}

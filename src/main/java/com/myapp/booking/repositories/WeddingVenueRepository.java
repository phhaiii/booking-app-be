package com.myapp.booking.repositories;

import com.myapp.booking.models.WeddingVenues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeddingVenueRepository extends JpaRepository<WeddingVenues, Long> {

    // 🔹 Tìm theo slug (thường dùng cho trang chi tiết)
    Optional<WeddingVenues> findBySlugAndDeletedAtIsNull(String slug);

    // 🔹 Tìm theo tên, có thể dùng để tìm kiếm
    List<WeddingVenues> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    // 🔹 Tìm tất cả địa điểm còn hoạt động
    List<WeddingVenues> findByIsAvailableTrueAndDeletedAtIsNull();

    // 🔹 Tìm tất cả địa điểm của một vendor cụ thể
    List<WeddingVenues> findByVendorIdAndDeletedAtIsNull(Long vendorId);

    // 🔹 Kiểm tra trùng tên hoặc slug
    Boolean existsByName(String name);
    Boolean existsBySlug(String slug);
}

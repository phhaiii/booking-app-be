package com.myapp.booking.dtos.responses;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class WeddingVenueResponse {
    private Long id;
    private Long vendorId;
    private String vendorName;        // Tên người cho thuê (nếu muốn hiển thị)
    private String name;
    private String slug;
    private String description;
    private String address;
    private String city;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacity;
    private BigDecimal pricePerTable;
    private BigDecimal depositPercentage;
    private List<String> images;
    private List<String> amenities;
    private Boolean isAvailable;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
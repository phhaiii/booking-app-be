package com.myapp.booking.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private Long id;
    private String name;
    private String description;

    // ✅ MAIN FIELDS
    private BigDecimal price; // Giá tổng menu
    private Integer guestsPerTable; // Số khách/bàn
    private BigDecimal pricePerPerson; // Giá/người (calculated)

    // ✅ Menu items
    private List<String> items;
    private Integer itemCount;

    private Boolean isActive;

    // Post info
    private Long postId;
    private String postTitle;

    // Vendor info
    private Long vendorId;
    private String vendorName;
    private String vendorEmail;
    private String vendorPhone;
    private String category;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ Formatted values
    private String formattedPrice;
    private String formattedPricePerPerson;
}
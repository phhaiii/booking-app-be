package com.myapp.booking.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeddingVenueRequest {

    @NotNull(message = "Vendor ID không được để trống")
    private Long vendorId;

    @NotBlank(message = "Tên sảnh cưới không được để trống")
    @Size(max = 100, message = "Tên sảnh cưới không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 120, message = "Slug không được vượt quá 120 ký tự")
    private String slug;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 100, message = "Tên thành phố không được vượt quá 100 ký tự")
    private String city;

    @Size(max = 100, message = "Tên quận/huyện không được vượt quá 100 ký tự")
    private String district;

    @Digits(integer = 2, fraction = 8, message = "Vĩ độ không hợp lệ")
    private BigDecimal latitude;

    @Digits(integer = 3, fraction = 8, message = "Kinh độ không hợp lệ")
    private BigDecimal longitude;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    @NotNull(message = "Giá mỗi bàn không được để trống")
    @DecimalMin(value = "0.00", inclusive = false, message = "Giá mỗi bàn phải lớn hơn 0")
    private BigDecimal pricePerTable;

    @DecimalMin(value = "0.00", message = "Phần trăm đặt cọc không được âm")
    @DecimalMax(value = "100.00", message = "Phần trăm đặt cọc không được vượt quá 100%")
    private BigDecimal depositPercentage;

    private List<@NotBlank(message = "Đường dẫn hình ảnh không được trống") String> images;

    private List<@NotBlank(message = "Tên tiện nghi không được trống") String> amenities;

    private Boolean isAvailable;
}

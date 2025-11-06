package com.myapp.booking.dtos.requests.post;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {

    @Size(min = 10, max = 100, message = "Tiêu đề phải từ 10-100 ký tự")
    private String title;

    @Size(min = 20, max = 200, message = "Mô tả phải từ 20-200 ký tự")
    private String description;

    @Size(min = 50, max = 1000, message = "Nội dung phải từ 50-1000 ký tự")
    private String content;

    private String location;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    private String style;

    @Size(max = 10, message = "Tối đa 10 hình ảnh")
    private List<String> images;

    private Set<String> amenities;

    private Boolean allowComments;

    private Boolean enableNotifications;
}
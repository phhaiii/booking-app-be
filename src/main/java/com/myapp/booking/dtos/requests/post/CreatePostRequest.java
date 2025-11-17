package com.myapp.booking.dtos.requests.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePostRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 100, message = "Tiêu đề phải từ 10-100 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 20, max = 200, message = "Mô tả phải từ 20-200 ký tự")
    private String description;

    @Size(min = 50, max = 1000, message = "Nội dung phải từ 50-1000 ký tự")
    private String content;

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    private String style;

    @NotEmpty(message = "Phải có ít nhất 1 hình ảnh")
    private List<MultipartFile> images;

    private Set<String> amenities;

    @Builder.Default
    private Boolean allowComments = true;

    @Builder.Default
    private Boolean enableNotifications = true;
}

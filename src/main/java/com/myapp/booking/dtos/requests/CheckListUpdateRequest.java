package com.myapp.booking.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckListUpdateRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 1, max = 200, message = "Tiêu đề phải từ 1-200 ký tự")
    private String title;

    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;
}
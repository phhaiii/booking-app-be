package com.myapp.booking.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuRequest {

    @NotBlank(message = "Menu name is required")
    @Size(min = 3, max = 255, message = "Menu name must be between 3 and 255 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    // ✅ CHỈ 2 FIELD CHÍNH
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Invalid price format")
    private BigDecimal price; // Giá tổng menu

    @NotNull(message = "Guests per table is required")
    @Min(value = 1, message = "Guests per table must be at least 1")
    @Max(value = 20, message = "Guests per table cannot exceed 20")
    private Integer guestsPerTable;

    // ✅ Danh sách món ăn
    @NotEmpty(message = "Menu must have at least one item")
    @Size(min = 1, max = 50, message = "Menu must have between 1 and 50 items")
    private List<@NotBlank(message = "Item name cannot be blank") String> items;
}
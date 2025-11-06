package com.myapp.booking.dtos.requests.booking;

import com.myapp.booking.models.WeddingBooking;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingUpdateRequest {

    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String customerName;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String customerEmail;

    private WeddingBooking.ServiceType serviceType;

    @Future(message = "Ngày đặt phải là ngày trong tương lai")
    private LocalDateTime requestedDate;

    @Min(value = 1, message = "Số khách phải lớn hơn 0")
    private Integer numberOfGuests;

    @DecimalMin(value = "0.0", inclusive = false, message = "Ngân sách phải lớn hơn 0")
    private Double budget;

    @Size(max = 2000, message = "Lời nhắn không được vượt quá 2000 ký tự")
    private String message;
}
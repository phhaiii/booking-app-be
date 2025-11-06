package com.myapp.booking.dtos.requests.booking;

import com.myapp.booking.models.WeddingBooking;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String customerPhone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String customerEmail;

    private Long venueId;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 200, message = "Tên dịch vụ không được vượt quá 200 ký tự")
    private String serviceName;

    @NotNull(message = "Loại dịch vụ không được để trống")
    private WeddingBooking.ServiceType serviceType;

    @NotNull(message = "Ngày đặt không được để trống")
    @Future(message = "Ngày đặt phải là ngày trong tương lai")
    private LocalDateTime requestedDate;

    @Min(value = 1, message = "Số khách phải lớn hơn 0")
    private Integer numberOfGuests;

    @DecimalMin(value = "0.0", inclusive = false, message = "Ngân sách phải lớn hơn 0")
    private Double budget;

    @Size(max = 2000, message = "Lời nhắn không được vượt quá 2000 ký tự")
    private String message;

    private Long vendorId;
}
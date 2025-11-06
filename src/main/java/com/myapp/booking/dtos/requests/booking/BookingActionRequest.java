package com.myapp.booking.dtos.requests.booking;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingActionRequest {

    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String reason;

    private String note;
}
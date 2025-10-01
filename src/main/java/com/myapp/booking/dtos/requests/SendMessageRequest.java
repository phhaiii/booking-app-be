package com.myapp.booking.dtos.requests;

import com.myapp.booking.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @NotNull(message = "Admin ID không được để trống")
    private Long adminId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String messageText;

    private MessageType messageType = MessageType.TEXT;

    private String fileUrl;
}

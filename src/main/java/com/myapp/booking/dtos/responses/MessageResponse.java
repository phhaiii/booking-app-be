package com.myapp.booking.dtos.responses;

import com.myapp.booking.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String messageText;
    private MessageType messageType;
    private String file_url;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}

package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.SendMessageRequest;
import com.myapp.booking.dtos.responses.MessageResponse;
import com.myapp.booking.services.interfaces.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final IChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Nhận tin nhắn từ client và gửi đến người nhận qua WebSocket
     * Client gửi tin nhắn đến: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        String senderEmail = principal.getName();

        // Lưu tin nhắn vào database
        MessageResponse message = chatService.sendMessage(senderEmail, request);

        // Gửi tin nhắn đến admin qua WebSocket
        messagingTemplate.convertAndSendToUser(
                request.getAdminId().toString(),
                "/queue/messages",
                message
        );

        // Gửi lại cho người gửi để confirm
        messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/messages",
                message
        );
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     * Client gửi đến: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Long conversationId, Principal principal) {
        String userEmail = principal.getName();
        chatService.markMessagesAsRead(userEmail, conversationId);

        // Notify người gửi rằng tin nhắn đã được đọc
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId + ".read",
                conversationId
        );
    }
}
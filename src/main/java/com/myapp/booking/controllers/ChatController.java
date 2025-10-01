package com.myapp.booking.controllers;

import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.requests.SendMessageRequest;
import com.myapp.booking.dtos.responses.ConversationResponse;
import com.myapp.booking.dtos.responses.MessageResponse;
import com.myapp.booking.services.interfaces.IChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;

    /**
     * Tạo hoặc lấy conversation với admin
     */
    @PostMapping("/conversations/{adminId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateConversation(
            Authentication authentication,
            @PathVariable Long adminId
    ) {
        String userEmail = authentication.getName();
        ConversationResponse response = chatService.getOrCreateConversation(userEmail, adminId);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy cuộc trò chuyện thành công"));
    }

    /**
     * Lấy danh sách conversation
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        List<ConversationResponse> responses = chatService.getConversations(userEmail);
        return ResponseEntity.ok(ApiResponse.success(responses, "Lấy danh sách cuộc trò chuyện thành công"));
    }

    /**
     * Gửi tin nhắn
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            Authentication authentication,
            @Valid @RequestBody SendMessageRequest request
    ) {
        String userEmail = authentication.getName();
        MessageResponse response = chatService.sendMessage(userEmail, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Gửi tin nhắn thành công"));
    }

    /**
     * Lấy tin nhắn trong conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            Authentication authentication,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageResponse> responses = chatService.getMessages(userEmail, conversationId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses, "Lấy tin nhắn thành công"));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            Authentication authentication,
            @PathVariable Long conversationId
    ) {
        String userEmail = authentication.getName();
        chatService.markMessagesAsRead(userEmail, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đánh dấu tin nhắn là đã đọc"));
    }

    /**
     * Xóa conversation
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            Authentication authentication,
            @PathVariable Long conversationId
    ) {
        String userEmail = authentication.getName();
        chatService.deleteConversation(userEmail, conversationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa cuộc trò chuyện thành công"));
    }
}
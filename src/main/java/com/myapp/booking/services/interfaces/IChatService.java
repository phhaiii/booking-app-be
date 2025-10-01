package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.responses.ConversationResponse;
import com.myapp.booking.dtos.requests.SendMessageRequest;
import com.myapp.booking.dtos.responses.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IChatService {

    /**
     * Tạo hoặc lấy conversation giữa user và admin
     */
    ConversationResponse getOrCreateConversation(String userEmail, Long adminId);

    /**
     * Gửi tin nhắn
     */
    MessageResponse sendMessage(String senderEmail, SendMessageRequest request);

    /**
     * Lấy danh sách conversation của user
     */
    List<ConversationResponse> getConversations(String userEmail);

    /**
     * Lấy tin nhắn trong conversation với phân trang
     */
    Page<MessageResponse> getMessages(String userEmail, Long conversationId, Pageable pageable);

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    void markMessagesAsRead(String userEmail, Long conversationId);

    /**
     * Xóa conversation
     */
    void deleteConversation(String userEmail, Long conversationId);
}
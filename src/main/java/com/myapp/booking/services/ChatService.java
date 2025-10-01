package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.SendMessageRequest;
import com.myapp.booking.dtos.responses.ConversationResponse;
import com.myapp.booking.dtos.responses.MessageResponse;
import com.myapp.booking.exceptions.BadRequestException;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.models.Conversation;
import com.myapp.booking.models.Message;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.ConversationRepository;
import com.myapp.booking.repositories.MessageRepository;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.services.interfaces.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(String userEmail, Long adminId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin không tồn tại"));

        // Kiểm tra admin có role ADMIN không
        if (!"ADMIN".equals(admin.getRole().getRoleName())) {
            throw new BadRequestException("Chỉ có thể nhắn tin với Admin");
        }

        // Tìm hoặc tạo conversation
        Conversation conversation = conversationRepository.findByUserAndAdmin(user, admin)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .user(user)
                            .admin(admin)
                            .unreadCountUser(0)
                            .unreadCountAdmin(0)
                            .build();
                    return conversationRepository.save(newConv);
                });

        return mapToConversationResponse(conversation, user);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(String senderEmail, SendMessageRequest request) {
        User sender = userRepository.findByEmailAndDeletedAtIsNull(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        User admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin không tồn tại"));

        // Tìm hoặc tạo conversation
        Conversation conversation = conversationRepository.findByUserAndAdmin(sender, admin)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .user(sender)
                            .admin(admin)
                            .unreadCountUser(0)
                            .unreadCountAdmin(0)
                            .build();
                    return conversationRepository.save(newConv);
                });

        // Tạo tin nhắn mới
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .messageText(request.getMessageText())
                .messageType(request.getMessageType())
                .fileUrl(request.getFileUrl())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        // Cập nhật conversation
        conversation.setLastMessage(request.getMessageText());
        conversation.setLastMessageAt(LocalDateTime.now());

        // Tăng unread count cho người nhận
        if (sender.getId().equals(conversation.getUser().getId())) {
            conversation.setUnreadCountAdmin(conversation.getUnreadCountAdmin() + 1);
        } else {
            conversation.setUnreadCountUser(conversation.getUnreadCountUser() + 1);
        }

        conversationRepository.save(conversation);

        return mapToMessageResponse(message);
    }

    @Override
    public List<ConversationResponse> getConversations(String userEmail) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        List<Conversation> conversations = conversationRepository.findAllByUserId(user.getId());

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, user))
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageResponse> getMessages(String userEmail, Long conversationId, Pageable pageable) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuộc trò chuyện không tồn tại"));

        // Kiểm tra quyền truy cập
        if (!conversation.getUser().getId().equals(user.getId()) &&
                !conversation.getAdmin().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập cuộc trò chuyện này");
        }

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, pageable);

        return messages.map(this::mapToMessageResponse);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String userEmail, Long conversationId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuộc trò chuyện không tồn tại"));

        // Kiểm tra quyền truy cập
        if (!conversation.getUser().getId().equals(user.getId()) &&
                !conversation.getAdmin().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền truy cập cuộc trò chuyện này");
        }

        // Đánh dấu tin nhắn đã đọc
        messageRepository.markAllAsRead(conversationId, user.getId());

        // Reset unread count
        if (user.getId().equals(conversation.getUser().getId())) {
            conversation.setUnreadCountUser(0);
        } else {
            conversation.setUnreadCountAdmin(0);
        }

        conversationRepository.save(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(String userEmail, Long conversationId) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuộc trò chuyện không tồn tại"));

        // Kiểm tra quyền truy cập
        if (!conversation.getUser().getId().equals(user.getId()) &&
                !conversation.getAdmin().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền xóa cuộc trò chuyện này");
        }

        conversationRepository.delete(conversation);
    }

    // Helper methods
    private ConversationResponse mapToConversationResponse(Conversation conversation, User currentUser) {
        // Xác định người còn lại trong conversation
        User otherUser = conversation.getUser().getId().equals(currentUser.getId())
                ? conversation.getAdmin()
                : conversation.getUser();

        Integer unreadCount = currentUser.getId().equals(conversation.getUser().getId())
                ? conversation.getUnreadCountUser()
                : conversation.getUnreadCountAdmin();

        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .userName(conversation.getUser().getFullName())
                .userAvatar(conversation.getUser().getAvatarUrl())
                .adminId(conversation.getAdmin().getId())
                .adminName(conversation.getAdmin().getFullName())
                .adminAvatar(conversation.getAdmin().getAvatarUrl())
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(unreadCount)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private MessageResponse mapToMessageResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderAvatar(message.getSender().getAvatarUrl())
                .messageText(message.getMessageText())
                .messageType(message.getMessageType())
                .file_url(message.getFileUrl())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
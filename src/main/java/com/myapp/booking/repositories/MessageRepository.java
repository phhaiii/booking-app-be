package com.myapp.booking.repositories;

import com.myapp.booking.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy danh sách tin nhắn trong 1 conversation, sort theo thời gian mới nhất
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    // Đánh dấu tất cả tin nhắn là đã đọc (trừ tin do user đó gửi)
    @Modifying
    @Query("UPDATE Message m " +
            "SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
            "WHERE m.conversation.id = :conversationId " +
            "AND m.sender.id <> :userId " +
            "AND m.isRead = false")
    void markAllAsRead(@Param("conversationId") Long conversationId,
                       @Param("userId") Long userId);
}

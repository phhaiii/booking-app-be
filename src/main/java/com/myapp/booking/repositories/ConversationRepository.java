package com.myapp.booking.repositories;

import com.myapp.booking.models.Conversation;
import com.myapp.booking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUserAndAdmin(User user, User admin);

    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId OR c.admin.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    List<Conversation> findByUserIdOrderByLastMessageAtDesc(Long userId);

    List<Conversation> findByAdminIdOrderByLastMessageAtDesc(Long adminId);

    boolean existsByUserIdAndAdminId(Long userId, Long adminId);
}

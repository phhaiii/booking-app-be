package com.myapp.booking.repositories;

import com.myapp.booking.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find by token
    Optional<PasswordResetToken> findByToken(String token);

    // Find valid token by user
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user.id = :userId " +
           "AND t.isUsed = false AND t.expiresAt > :now ORDER BY t.createdAt DESC")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Find by user
    List<PasswordResetToken> findByUserId(Long userId);

    // Delete expired tokens
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    // Mark all user tokens as used
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true, t.usedAt = :now WHERE t.user.id = :userId AND t.isUsed = false")
    void markAllUserTokensAsUsed(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}


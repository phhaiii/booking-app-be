package com.myapp.booking.services;

import com.myapp.booking.models.PasswordResetToken;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Create password reset token
     */
    @Transactional
    public PasswordResetToken createToken(User user) {
        // Mark all existing tokens as used
        passwordResetTokenRepository.markAllUserTokensAsUsed(user.getId(), LocalDateTime.now());

        // Create new token (valid for 1 hour)
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        return passwordResetTokenRepository.save(token);
    }

    /**
     * Validate token
     */
    public Optional<PasswordResetToken> validateToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValid);
    }

    /**
     * Mark token as used
     */
    @Transactional
    public void markTokenAsUsed(String token) {
        passwordResetTokenRepository.findByToken(token)
                .ifPresent(t -> {
                    t.markAsUsed();
                    passwordResetTokenRepository.save(t);
                });
    }

    /**
     * Clean up expired tokens (should be run periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}


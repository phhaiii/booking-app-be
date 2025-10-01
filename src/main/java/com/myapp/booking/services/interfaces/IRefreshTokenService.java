package com.myapp.booking.services.interfaces;

import com.myapp.booking.models.RefreshToken;
import com.myapp.booking.models.User;

public interface IRefreshTokenService {
    RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByToken(String token);
    void deleteByUser(User user);
    void deleteExpiredTokens();
}

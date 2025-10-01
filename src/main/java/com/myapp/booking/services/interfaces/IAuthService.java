package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.reponses.AuthResponse;
import com.myapp.booking.dtos.requests.LoginRequest;
import com.myapp.booking.dtos.requests.RefreshTokenRequest;
import com.myapp.booking.dtos.requests.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
}

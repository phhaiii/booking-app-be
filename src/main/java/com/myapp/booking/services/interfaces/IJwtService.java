package com.myapp.booking.services.interfaces;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    String extractUsername(String token);
    Claims extractAllClaims(String token);
    Boolean isTokenValid(String token, UserDetails userDetails);
    Boolean isTokenExpired(String token);
}

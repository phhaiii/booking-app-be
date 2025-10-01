package com.myapp.booking.services;

import com.myapp.booking.dtos.reponses.AuthResponse;
import com.myapp.booking.dtos.reponses.UserResponse;
import com.myapp.booking.dtos.requests.LoginRequest;
import com.myapp.booking.dtos.requests.RefreshTokenRequest;
import com.myapp.booking.dtos.requests.RegisterRequest;
import com.myapp.booking.exceptions.AccountLockedException;
import com.myapp.booking.exceptions.InvalidCredentialsException;
import com.myapp.booking.exceptions.TokenRefreshException;
import com.myapp.booking.exceptions.UserAlreadyExistsException;
import com.myapp.booking.models.RefreshToken;
import com.myapp.booking.models.Role;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.RefreshTokenRepository;
import com.myapp.booking.repositories.RoleRepository;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.security.CustomUserDetails;
import com.myapp.booking.services.interfaces.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest httpServletRequest;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 15 * 60 * 1000; // 15 phút

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã được sử dụng!");
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Số điện thoại đã được sử dụng!");
        }

        // Lấy role mặc định (CUSTOMER)
        Role customerRole = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER không tồn tại!"));

        // Tạo user mới
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .role(customerRole)
                .isActive(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);

        // Tạo UserDetails
        CustomUserDetails userDetails = CustomUserDetails.build(user);

        // Tạo tokens
        String accessToken = jwtService.generateAccessToken(userDetails);

        // Lấy thông tin device
        String deviceInfo = getDeviceInfo();
        String ipAddress = getClientIP();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user, deviceInfo, ipAddress
        );

        // Tạo response
        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoặc mật khẩu không đúng!"));

        // Kiểm tra tài khoản bị khóa
        if (user.getIsLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException(
                        "Tài khoản đã bị khóa. Vui lòng thử lại sau!"
                );
            } else {
                // Mở khóa tài khoản nếu đã hết thời gian khóa
                user.setIsLocked(false);
                user.setLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }

        // Kiểm tra tài khoản không hoạt động
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Tài khoản đã bị vô hiệu hóa!");
        }

        try {
            // Xác thực
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Reset số lần đăng nhập thất bại
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }

            // Cập nhật thời gian đăng nhập
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Tạo tokens
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);

            String deviceInfo = getDeviceInfo();
            String ipAddress = getClientIP();

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                    user, deviceInfo, ipAddress
            );

            return buildAuthResponse(user, accessToken, refreshToken.getToken());

        } catch (BadCredentialsException e) {
            // Tăng số lần đăng nhập thất bại
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setIsLocked(true);
                user.setLockedUntil(LocalDateTime.now().plusSeconds(LOCK_TIME_DURATION / 1000));
                userRepository.save(user);
                throw new AccountLockedException(
                        "Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần!"
                );
            }

            userRepository.save(user);
            throw new InvalidCredentialsException(
                    String.format("Email hoặc mật khẩu không đúng! Còn %d lần thử.",
                            MAX_FAILED_ATTEMPTS - attempts)
            );
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token không hợp lệ!"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = CustomUserDetails.build(user);

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return buildAuthResponse(user, newAccessToken, requestRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String refreshToken = token.substring(7);
            refreshTokenService.deleteByToken(refreshToken);
        }
    }

    // Helper methods
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .avatarUrl(user.getAvatarUrl())
                .roleName(user.getRole().getRoleName())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours
                .user(userResponse)
                .build();
    }

    private String getDeviceInfo() {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown Device";
    }

    private String getClientIP() {
        String xfHeader = httpServletRequest.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return httpServletRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

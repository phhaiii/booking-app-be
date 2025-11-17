package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.ChangePasswordRequest;
import com.myapp.booking.dtos.requests.UpdateProfileRequest;
import com.myapp.booking.dtos.responses.RoleResponse;
import com.myapp.booking.dtos.responses.UserResponse;
import com.myapp.booking.exceptions.UserAlreadyExistsException;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.services.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserAlreadyExistsException("Người dùng không tồn tại"));
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserAlreadyExistsException("Người dùng không tồn tại"));

        if (user.getDeletedAt() != null) {
            throw new UserAlreadyExistsException("Người dùng không tồn tại");
        }

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) throws BadRequestException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserAlreadyExistsException("Người dùng không tồn tại"));

        // Kiểm tra số điện thoại đã tồn tại
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone((request.getPhone()))) {
                throw new BadRequestException("Số điện thoại đã được sử dụng");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) throws BadRequestException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserAlreadyExistsException("Người dùng không tồn tại"));

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        // Kiểm tra mật khẩu mới không giống mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UserAlreadyExistsException("Người dùng không tồn tại"));

        // Soft delete
        user.setDeletedAt(LocalDateTime.now());
        user.setIsActive(false);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        // Build role response
        RoleResponse roleResponse = null;
        if (user.getRole() != null) {
            roleResponse = RoleResponse.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getRoleName() != null 
                            ? user.getRole().getRoleName().name() 
                            : null)
                    .build();
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(roleResponse)
                .avatar(user.getAvatarUrl())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
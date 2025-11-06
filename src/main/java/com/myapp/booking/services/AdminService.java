package com.myapp.booking.services;

import com.myapp.booking.dtos.responses.UserResponse;
import com.myapp.booking.enums.RoleName;
import com.myapp.booking.exceptions.ResourceNotFoundException;
import com.myapp.booking.models.Role;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.RoleRepository;
import com.myapp.booking.repositories.UserRepository;
import com.myapp.booking.services.interfaces.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", userRepository.countByRole_RoleName(RoleName.ADMIN));
        stats.put("totalVendors", userRepository.countByRole_RoleName(RoleName.VENDOR));
        stats.put("totalCustomers", userRepository.countByRole_RoleName(RoleName.USER));
        stats.put("activeUsers", userRepository.countByIsActiveTrue());
        stats.put("lockedUsers", userRepository.countByIsLockedTrue());

        return stats;
    }

    @Override
    public Page<UserResponse> getAllUsers(String search, String roleFilter, Pageable pageable) {
        Page<User> users;

        if (search != null && !search.isEmpty()) {
            users = userRepository.findByFullNameContainingOrEmailContaining(search, search, pageable);
        } else if (roleFilter != null && !roleFilter.isEmpty()) {
            RoleName roleName;
            try {
                roleName = RoleName.valueOf(roleFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Vai trò không hợp lệ: " + roleFilter);
            }
            users = userRepository.findByRole_RoleName(roleName, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void toggleLockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Boolean locked = user.getIsLocked();
        user.setIsLocked(locked == null ? Boolean.TRUE : !locked);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleActivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        Boolean active = user.getIsActive();
        user.setIsActive(active == null ? Boolean.FALSE : !active);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserRole(Long userId, String roleName) {
        // Chuyển chuỗi roleName thành enum an toàn
        RoleName enumRole = parseRoleName(roleName);

        // Gọi repository đúng kiểu enum (vì findByRoleName nhận RoleName)
        Role role = roleRepository.findByRoleName(enumRole)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò: " + roleName));

        // Tìm user và cập nhật vai trò
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        user.setRole(role);
        userRepository.save(user);
    }

    private RoleName parseRoleName(String roleName) {
        try {
            return RoleName.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Vai trò không hợp lệ: " + roleName);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .dateOfBirth(user.getDateOfBirth())
                // use roleName builder method and convert enum to String safely
                .roleName(user.getRole() != null && user.getRole().getRoleName() != null
                        ? user.getRole().getRoleName().name()
                        : null)
                .avatarUrl(user.getAvatarUrl())
                .isActive(user.getIsActive())
                .isLocked(user.getIsLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
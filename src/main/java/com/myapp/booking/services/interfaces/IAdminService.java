package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface IAdminService {
    Map<String, Object> getDashboardStats();
    Page<UserResponse> getAllUsers(String search, String role, Pageable pageable);
    UserResponse getUserById(Long id);
    void toggleLockUser(Long id);
    void toggleActivateUser(Long id);
    void deleteUser(Long id);
    void changeUserRole(Long id, String role);
}
package com.myapp.booking.controllers;

import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.responses.UserResponse;
import com.myapp.booking.services.interfaces.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final IAdminService adminService;

    /**
     * Dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        Map<String, Object> stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê thành công"));
    }

    /**
     * Get all users with pagination
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Pageable pageable
    ) {
        Page<UserResponse> users = adminService.getAllUsers(search, role, pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công"));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công"));
    }

    /**
     * Lock/Unlock user
     */
    @PutMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> toggleLockUser(@PathVariable Long id) {
        adminService.toggleLockUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Thay đổi trạng thái khóa thành công"));
    }

    /**
     * Activate/Deactivate user
     */
    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> toggleActivateUser(@PathVariable Long id) {
        adminService.toggleActivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Thay đổi trạng thái kích hoạt thành công"));
    }

    /**
     * Delete user (soft delete)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa người dùng thành công"));
    }

    /**
     * Change user role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> changeUserRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.success(null, "Thay đổi vai trò thành công"));
    }
}
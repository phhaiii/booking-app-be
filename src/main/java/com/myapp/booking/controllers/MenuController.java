package com.myapp.booking.controllers;

import com.myapp.booking.configurations.SecurityUtils;
import com.myapp.booking.dtos.requests.CreateMenuRequest;
import com.myapp.booking.dtos.requests.UpdateMenuRequest;
import com.myapp.booking.dtos.responses.ApiResponse;
import com.myapp.booking.dtos.responses.MenuResponse;
import com.myapp.booking.services.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts/{postId}/menus")
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "APIs for managing post menus")
public class MenuController {

    private final MenuService menuService;

    // ═══════════════════════════════════════════════════════════════
    // CREATE MENU
    // ═══════════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Create menu", description = "Add menu to post")
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @PathVariable Long postId,
            @Valid @RequestBody CreateMenuRequest request,
            Authentication authentication) {

        log.info("📝 POST /api/posts/{}/menus - Create menu", postId);
        log.info("Request: {}", request);

        Long userId = SecurityUtils.getUserId(authentication);
        MenuResponse response = menuService.createMenu(request, postId, userId);

        log.info("✅ Menu created successfully: {}", response.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Menu created successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // GET MENUS BY POST
    // ═══════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(summary = "Get menus by post", description = "Get all menus of a post")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByPost(
            @PathVariable Long postId) {

        log.info("📋 GET /api/posts/{}/menus - Get menus by post", postId);

        List<MenuResponse> menus = menuService.getMenusByPost(postId);

        log.info("✅ Found {} menus for post {}", menus.size(), postId);

        return ResponseEntity.ok(
                ApiResponse.success(menus, "Menus retrieved successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // GET MENU BY ID
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/{menuId}")
    @Operation(summary = "Get menu by ID", description = "Get menu details")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuById(
            @PathVariable Long postId,
            @PathVariable Long menuId) {

        log.info("🔍 GET /api/posts/{}/menus/{} - Get menu by ID", postId, menuId);

        MenuResponse menu = menuService.getMenuById(menuId);

        log.info("✅ Found menu: {}", menu.getName());

        return ResponseEntity.ok(
                ApiResponse.success(menu, "Menu retrieved successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE MENU
    // ═══════════════════════════════════════════════════════════════

    @PutMapping("/{menuId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Update menu", description = "Update menu details")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable Long postId,
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateMenuRequest request,
            Authentication authentication) {

        log.info("✏️ PUT /api/posts/{}/menus/{} - Update menu", postId, menuId);
        log.info("Request: {}", request);

        Long userId = SecurityUtils.getUserId(authentication);
        MenuResponse response = menuService.updateMenu(menuId, request, userId);

        log.info("✅ Menu updated successfully: {}", response.getName());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Menu updated successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE MENU
    // ═══════════════════════════════════════════════════════════════

    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    @Operation(summary = "Delete menu", description = "Soft delete menu from post")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable Long postId,
            @PathVariable Long menuId,
            Authentication authentication) {

        log.info("🗑️ DELETE /api/posts/{}/menus/{} - Delete menu", postId, menuId);

        Long userId = SecurityUtils.getUserId(authentication);
        menuService.deleteMenu(menuId, userId);

        log.info("✅ Menu deleted successfully");

        return ResponseEntity.ok(
                ApiResponse.success(null, "Menu deleted successfully"));
    }
}
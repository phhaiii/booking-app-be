package com.myapp.booking.controllers;

import com.myapp.booking.dtos.requests.CheckListCreateRequest;
import com.myapp.booking.dtos.requests.CheckListUpdateRequest;
import com.myapp.booking.dtos.responses.CheckListResponse;
import com.myapp.booking.services.CheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class CheckListController {

    private final CheckListService service;

    /**
     * GET /api/checklists - Lấy tất cả items
     */
    @GetMapping
    public ResponseEntity<List<CheckListResponse>> getAllItems() {
        log.info("📥 GET /api/checklists - Get all items");

        try {
            List<CheckListResponse> items = service.getAllItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("❌ Error getting all items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/checklists/{id} - Lấy item theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CheckListResponse> getItemById(@PathVariable String id) {
        log.info("📥 GET /api/checklists/{} - Get item by id", id);

        try {
            CheckListResponse item = service.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            log.error("❌ Error getting item by id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/checklists - Tạo mới item
     */
    @PostMapping
    public ResponseEntity<CheckListResponse> createItem(
            @Valid @RequestBody CheckListCreateRequest request) {
        log.info("📥 POST /api/checklists - Create new item: {}", request.getTitle());

        try {
            CheckListResponse created = service.createItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("❌ Error creating item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/checklists/{id} - Cập nhật item
     */
    @PutMapping("/{id}")
    public ResponseEntity<CheckListResponse> updateItem(
            @PathVariable String id,
            @Valid @RequestBody CheckListUpdateRequest request) {
        log.info("📥 PUT /api/checklists/{} - Update item", id);

        try {
            CheckListResponse updated = service.updateItem(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("❌ Error updating item {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/checklists/{id} - Xóa item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        log.info("📥 DELETE /api/checklists/{} - Delete item", id);

        try {
            service.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("❌ Error deleting item {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PATCH /api/checklists/{id}/toggle - Toggle completed status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CheckListResponse> toggleCompleted(@PathVariable String id) {
        log.info("📥 PATCH /api/checklists/{}/toggle - Toggle completed", id);

        try {
            CheckListResponse toggled = service.toggleCompleted(id);
            return ResponseEntity.ok(toggled);
        } catch (RuntimeException e) {
            log.error("❌ Error toggling item {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/checklists/completed - Lấy items đã hoàn thành
     */
    @GetMapping("/completed")
    public ResponseEntity<List<CheckListResponse>> getCompletedItems() {
        log.info("📥 GET /api/checklists/completed - Get completed items");

        try {
            List<CheckListResponse> items = service.getCompletedItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("❌ Error getting completed items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/checklists/incomplete - Lấy items chưa hoàn thành
     */
    @GetMapping("/incomplete")
    public ResponseEntity<List<CheckListResponse>> getIncompleteItems() {
        log.info("📥 GET /api/checklists/incomplete - Get incomplete items");

        try {
            List<CheckListResponse> items = service.getIncompleteItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("❌ Error getting incomplete items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/checklists/statistics - Lấy thống kê
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        log.info("📥 GET /api/checklists/statistics - Get statistics");

        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("completed", service.countCompleted());
            stats.put("incomplete", service.countIncomplete());
            stats.put("total", service.countCompleted() + service.countIncomplete());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("❌ Error getting statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/checklists/search?title={title} - Tìm kiếm theo title
     */
    @GetMapping("/search")
    public ResponseEntity<List<CheckListResponse>> searchByTitle(
            @RequestParam String title) {
        log.info("📥 GET /api/checklists/search?title={}", title);

        try {
            List<CheckListResponse> items = service.searchByTitle(title);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("❌ Error searching by title '{}': {}", title, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
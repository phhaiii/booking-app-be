package com.myapp.booking.services;

import com.myapp.booking.dtos.requests.CheckListCreateRequest;
import com.myapp.booking.dtos.requests.CheckListUpdateRequest;
import com.myapp.booking.dtos.responses.CheckListResponse;
import com.myapp.booking.models.CheckListItem;
import com.myapp.booking.repositories.CheckListRepository;
import com.myapp.booking.services.interfaces.ICheckListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckListService implements ICheckListService {

    private final CheckListRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<CheckListResponse> getAllItems() {
        log.info("📋 Fetching all checklist items");

        List<CheckListItem> items = repository.findAllByOrderByCreatedAtDesc();

        log.info("✅ Found {} items", items.size());

        return items.stream()
                .map(CheckListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CheckListResponse getItemById(String id) {
        log.info("🔍 Fetching checklist item with id: {}", id);

        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Item not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy mục với ID: " + id);
                });

        log.info("✅ Found item: {}", item.getTitle());

        return CheckListResponse.fromEntity(item);
    }

    @Override
    public CheckListResponse createItem(CheckListCreateRequest request) {
        log.info("➕ Creating new checklist item: {}", request.getTitle());

        CheckListItem item = CheckListItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isCompleted(false)
                .build();

        CheckListItem saved = repository.save(item);

        log.info("✅ Created item with id: {}", saved.getId());

        return CheckListResponse.fromEntity(saved);
    }

    @Override
    public CheckListResponse updateItem(String id, CheckListUpdateRequest request) {
        log.info("✏️ Updating checklist item: {}", id);

        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Item not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy mục với ID: " + id);
                });

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());

        CheckListItem updated = repository.save(item);

        log.info("✅ Updated item: {}", updated.getTitle());

        return CheckListResponse.fromEntity(updated);
    }

    @Override
    public void deleteItem(String id) {
        log.info("🗑️ Deleting checklist item: {}", id);

        if (!repository.existsById(id)) {
            log.error("❌ Item not found with id: {}", id);
            throw new RuntimeException("Không tìm thấy mục với ID: " + id);
        }

        repository.deleteById(id);

        log.info("✅ Deleted item with id: {}", id);
    }

    @Override
    public CheckListResponse toggleCompleted(String id) {
        log.info("🔄 Toggling completed status for item: {}", id);

        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Item not found with id: {}", id);
                    return new RuntimeException("Không tìm thấy mục với ID: " + id);
                });

        item.toggleCompleted();

        CheckListItem updated = repository.save(item);

        log.info("✅ Toggled item: {} - completed: {}",
                updated.getTitle(), updated.getIsCompleted());

        return CheckListResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckListResponse> getCompletedItems() {
        log.info("✅ Fetching completed items");

        List<CheckListItem> items = repository.findByIsCompletedTrue();

        log.info("📊 Found {} completed items", items.size());

        return items.stream()
                .map(CheckListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckListResponse> getIncompleteItems() {
        log.info("⏳ Fetching incomplete items");

        List<CheckListItem> items = repository.findByIsCompletedFalse();

        log.info("📊 Found {} incomplete items", items.size());

        return items.stream()
                .map(CheckListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompleted() {
        long count = repository.countCompleted();
        log.info("📊 Completed items count: {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public long countIncomplete() {
        long count = repository.countIncomplete();
        log.info("📊 Incomplete items count: {}", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckListResponse> searchByTitle(String title) {
        log.info("🔍 Searching items by title: {}", title);

        List<CheckListItem> items = repository.findByTitleContainingIgnoreCase(title);

        log.info("📊 Found {} items matching '{}'", items.size(), title);

        return items.stream()
                .map(CheckListResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
package com.myapp.booking.services.interfaces;

import com.myapp.booking.dtos.requests.CheckListCreateRequest;
import com.myapp.booking.dtos.requests.CheckListUpdateRequest;
import com.myapp.booking.dtos.responses.CheckListResponse;

import java.util.List;

public interface ICheckListService {

    /**
     * Lấy tất cả checklist items
     */
    List<CheckListResponse> getAllItems();

    /**
     * Lấy checklist item theo ID
     */
    CheckListResponse getItemById(String id);

    /**
     * Tạo mới checklist item
     */
    CheckListResponse createItem(CheckListCreateRequest request);

    /**
     * Cập nhật checklist item
     */
    CheckListResponse updateItem(String id, CheckListUpdateRequest request);

    /**
     * Xóa checklist item
     */
    void deleteItem(String id);

    /**
     * Toggle trạng thái completed
     */
    CheckListResponse toggleCompleted(String id);

    /**
     * Lấy tất cả items đã hoàn thành
     */
    List<CheckListResponse> getCompletedItems();

    /**
     * Lấy tất cả items chưa hoàn thành
     */
    List<CheckListResponse> getIncompleteItems();

    /**
     * Đếm số items đã hoàn thành
     */
    long countCompleted();

    /**
     * Đếm số items chưa hoàn thành
     */
    long countIncomplete();

    /**
     * Tìm kiếm theo title
     */
    List<CheckListResponse> searchByTitle(String title);
}
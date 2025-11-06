package com.myapp.booking.repositories;

import com.myapp.booking.models.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckListRepository extends JpaRepository<CheckListItem, String> {

    // Tìm tất cả items đã hoàn thành
    List<CheckListItem> findByIsCompletedTrue();

    // Tìm tất cả items chưa hoàn thành
    List<CheckListItem> findByIsCompletedFalse();

    // Đếm số items đã hoàn thành
    @Query("SELECT COUNT(c) FROM CheckListItem c WHERE c.isCompleted = true")
    long countCompleted();

    // Đếm số items chưa hoàn thành
    @Query("SELECT COUNT(c) FROM CheckListItem c WHERE c.isCompleted = false")
    long countIncomplete();

    // Tìm kiếm theo title
    List<CheckListItem> findByTitleContainingIgnoreCase(String title);

    // Sắp xếp theo createdAt giảm dần
    List<CheckListItem> findAllByOrderByCreatedAtDesc();

    // Sắp xếp theo completedAt giảm dần
    List<CheckListItem> findAllByOrderByCompletedAtDesc();
}
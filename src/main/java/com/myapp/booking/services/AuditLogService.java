package com.myapp.booking.services;

import com.myapp.booking.models.AuditLog;
import com.myapp.booking.models.User;
import com.myapp.booking.repositories.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an action
     */
    @Transactional
    public void log(User user, String action, String tableName, Long recordId,
                    Object oldValues, Object newValues, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .tableName(tableName)
                    .recordId(recordId)
                    .oldValues(oldValues != null ? objectMapper.writeValueAsString(oldValues) : null)
                    .newValues(newValues != null ? objectMapper.writeValueAsString(newValues) : null)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Get audit logs by user
     */
    public Page<AuditLog> getLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * Get audit logs by table and record
     */
    public Page<AuditLog> getLogsByTableAndRecord(String tableName, Long recordId, Pageable pageable) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId)
                .stream()
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                ));
    }

    /**
     * Get audit logs by date range
     */
    public Page<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Get recent audit logs
     */
    public Page<AuditLog> getRecentLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}



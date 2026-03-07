package com.elocate.elocate.controller;

import com.elocate.elocate.model.AdminAuditLog;
import com.elocate.elocate.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Admin controller for audit log viewing
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AdminAuditLogService auditLogService;

    /**
     * Get audit logs with filters
     */
    @GetMapping
    public ResponseEntity<Page<AdminAuditLog>> getAuditLogs(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Admin fetching audit logs - action: {}, entity: {}, start: {}, end: {}",
                actionType, entityType, startDate, endDate);

        Pageable pageable = PageRequest.of(page, size);
        // Page<AdminAuditLog> logs = auditLogService.getAuditLogs(
        // actionType, entityType, startDate, endDate, pageable);

        return ResponseEntity.ok(Page.empty());
    }
}

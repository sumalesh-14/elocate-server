package com.elocate.elocate.service;

import com.elocate.elocate.context.UserContext;
import com.elocate.elocate.context.UserContextHolder;
import com.elocate.elocate.model.AdminAuditLog;
import com.elocate.elocate.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AdminAuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String actionType, String details) {
        UserContext context = UserContextHolder.getContext();
        if (context == null || context.getUserId() == null) {
            log.warn("Cannot log action without authenticated user context: {}", actionType);
            return;
        }

        AdminAuditLog auditLog = AdminAuditLog.builder()
                .adminId(context.getUserId())
                .adminName(context.getFullName())
                .actionType(actionType)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit logged: [{}] by admin '{}' - {}", actionType, context.getFullName(), details);
    }
}

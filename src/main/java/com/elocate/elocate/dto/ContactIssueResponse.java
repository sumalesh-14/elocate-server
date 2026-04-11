package com.elocate.elocate.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ContactIssueResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String message;
    private String status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

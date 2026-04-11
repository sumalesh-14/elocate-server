package com.elocate.elocate.controller;

import com.elocate.elocate.dto.ContactIssueRequest;
import com.elocate.elocate.dto.ContactIssueResponse;
import com.elocate.elocate.service.ContactIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/contact-issues")
@RequiredArgsConstructor
public class ContactIssueController {

    private final ContactIssueService contactIssueService;

    /** Public endpoint — no auth required */
    @PostMapping
    public ResponseEntity<ContactIssueResponse> submit(@Valid @RequestBody ContactIssueRequest request) {
        log.info("POST /api/v1/contact-issues from {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(contactIssueService.submit(request));
    }

    /** Admin — list all issues */
    @GetMapping
    public ResponseEntity<Page<ContactIssueResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(contactIssueService.getAll(search, status, page, size));
    }

    /** Admin — update status / add notes */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactIssueResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String adminNotes = body.get("adminNotes");
        return ResponseEntity.ok(contactIssueService.updateStatus(id, status, adminNotes));
    }
}

package com.elocate.elocate.service;

import com.elocate.elocate.dto.ContactIssueRequest;
import com.elocate.elocate.dto.ContactIssueResponse;
import com.elocate.elocate.model.ContactIssue;
import com.elocate.elocate.repository.ContactIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactIssueService {

    private final ContactIssueRepository contactIssueRepository;
    private final EmailService emailService;

    @Transactional
    public ContactIssueResponse submit(ContactIssueRequest request) {
        ContactIssue issue = ContactIssue.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .message(request.getMessage())
                .status("OPEN")
                .build();
        ContactIssue saved = contactIssueRepository.save(issue);
        log.info("Contact issue submitted: id={}, email={}", saved.getId(), saved.getEmail());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactIssueResponse> getAll(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isBlank()) {
            return contactIssueRepository.searchIssues(search, status, pageable).map(this::mapToResponse);
        }
        if (status != null && !status.isBlank()) {
            return contactIssueRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(this::mapToResponse);
        }
        return contactIssueRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToResponse);
    }

    @Transactional
    public ContactIssueResponse updateStatus(UUID id, String status, String adminNotes) {
        ContactIssue issue = contactIssueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact issue not found: " + id));

        String previousStatus = issue.getStatus();
        issue.setStatus(status);
        if (adminNotes != null) issue.setAdminNotes(adminNotes);
        ContactIssue saved = contactIssueRepository.save(issue);

        // Send reply email when admin resolves the issue with a note
        if ("RESOLVED".equals(status) && !previousStatus.equals("RESOLVED")
                && adminNotes != null && !adminNotes.isBlank()) {
            log.info("Sending reply email to {} for contact issue {}", saved.getEmail(), saved.getId());
            emailService.sendContactIssueReplyEmail(saved.getEmail(), saved.getName(), saved.getMessage(), adminNotes);
        }

        return mapToResponse(saved);
    }

    private ContactIssueResponse mapToResponse(ContactIssue issue) {
        return ContactIssueResponse.builder()
                .id(issue.getId())
                .name(issue.getName())
                .email(issue.getEmail())
                .phone(issue.getPhone())
                .message(issue.getMessage())
                .status(issue.getStatus())
                .adminNotes(issue.getAdminNotes())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }
}

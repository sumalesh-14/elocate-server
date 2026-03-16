package com.elocate.elocate.controller;

import com.elocate.elocate.dto.CreateFeedbackDto;
import com.elocate.elocate.dto.FeedbackResponse;
import com.elocate.elocate.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * Submit feedback for a completed recycle request
     * Only citizens can submit feedback for their own completed requests
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @RequestParam UUID userId,
            @Valid @RequestBody CreateFeedbackDto request) {
        log.info("POST /api/v1/feedback - userId: {}, recycleRequestId: {}",
                userId, request.getRecycleRequestId());

        FeedbackResponse response = feedbackService.submitFeedback(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get feedback for a specific recycle request
     */
    @GetMapping("/request/{recycleRequestId}")
    public ResponseEntity<FeedbackResponse> getFeedbackByRequestId(
            @PathVariable UUID recycleRequestId) {
        log.info("GET /api/v1/feedback/request/{}", recycleRequestId);

        FeedbackResponse response = feedbackService.getFeedbackByRequestId(recycleRequestId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all feedback submitted by a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByUserId(
            @PathVariable UUID userId) {
        log.info("GET /api/v1/feedback/user/{}", userId);

        List<FeedbackResponse> responses = feedbackService.getFeedbackByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all feedback for a facility (admin/facility owner view)
     */
    @GetMapping("/facility/{facilityId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackByFacilityId(
            @PathVariable UUID facilityId) {
        log.info("GET /api/v1/feedback/facility/{}", facilityId);

        List<FeedbackResponse> responses = feedbackService.getFeedbackByFacilityId(facilityId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all feedback (admin view)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedback() {
        log.info("GET /api/v1/feedback/admin/all");

        List<FeedbackResponse> responses = feedbackService.getAllFeedback();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get average rating for a facility
     */
    @GetMapping("/facility/{facilityId}/average-rating")
    public ResponseEntity<Double> getAverageRatingByFacilityId(
            @PathVariable UUID facilityId) {
        log.info("GET /api/v1/feedback/facility/{}/average-rating", facilityId);

        Double averageRating = feedbackService.getAverageRatingByFacilityId(facilityId);
        return ResponseEntity.ok(averageRating);
    }

    /**
     * Get feedback count for a facility
     */
    @GetMapping("/facility/{facilityId}/count")
    public ResponseEntity<Long> getFeedbackCountByFacilityId(
            @PathVariable UUID facilityId) {
        log.info("GET /api/v1/feedback/facility/{}/count", facilityId);

        Long count = feedbackService.getFeedbackCountByFacilityId(facilityId);
        return ResponseEntity.ok(count);
    }
}

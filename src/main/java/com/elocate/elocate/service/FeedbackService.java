package com.elocate.elocate.service;

import com.elocate.elocate.dto.CreateFeedbackDto;
import com.elocate.elocate.dto.FeedbackResponse;
import com.elocate.elocate.exception.RecycleRequestNotFoundException;
import com.elocate.elocate.model.Feedback;
import com.elocate.elocate.model.RecycleRequest;
import com.elocate.elocate.model.RecycleStatus;
import com.elocate.elocate.repository.FeedbackRepository;
import com.elocate.elocate.repository.RecycleRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final RecycleRequestRepository recycleRequestRepository;

    /**
     * Submit feedback for a completed recycle request
     * Only allows feedback for COMPLETED requests
     */
    @Transactional
    public FeedbackResponse submitFeedback(UUID userId, CreateFeedbackDto request) {
        log.info("Submitting feedback for user: {}, recycleRequestId: {}, rating: {}",
                userId, request.getRecycleRequestId(), request.getRating());

        // Fetch recycle request
        RecycleRequest recycleRequest = recycleRequestRepository.findById(request.getRecycleRequestId())
                .orElseThrow(() -> new RecycleRequestNotFoundException(request.getRecycleRequestId()));

        // Validate request belongs to user
        if (!recycleRequest.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Request does not belong to user");
        }

        // Validate request is completed
        if (recycleRequest.getStatus() != RecycleStatus.RECYCLED) {
            throw new IllegalArgumentException("Feedback can only be submitted for Recycled requests");
        }

        // Check if feedback already exists
        if (feedbackRepository.findByRecycleRequestId(request.getRecycleRequestId()).isPresent()) {
            throw new IllegalArgumentException("Feedback already exists for this request");
        }

        // Create and save feedback
        Feedback feedback = Feedback.builder()
                .recycleRequest(recycleRequest)
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .category(request.getCategory())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Get feedback for a specific recycle request
     */
    public FeedbackResponse getFeedbackByRequestId(UUID recycleRequestId) {
        log.info("Fetching feedback for recycleRequestId: {}", recycleRequestId);

        Feedback feedback = feedbackRepository.findByRecycleRequestId(recycleRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found for request: " + recycleRequestId));

        return mapToResponse(feedback);
    }

    /**
     * Get all feedback submitted by a user
     */
    public List<FeedbackResponse> getFeedbackByUserId(UUID userId) {
        log.info("Fetching feedback for userId: {}", userId);

        List<Feedback> feedbacks = feedbackRepository.findByUserId(userId);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all feedback for a facility (admin view)
     */
    public List<FeedbackResponse> getFeedbackByFacilityId(UUID facilityId) {
        log.info("Fetching feedback for facilityId: {}", facilityId);

        List<Feedback> feedbacks = feedbackRepository.findByFacilityId(facilityId);
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all feedback (admin view)
     */
    public List<FeedbackResponse> getAllFeedback() {
        log.info("Fetching all feedback");

        List<Feedback> feedbacks = feedbackRepository.findAllOrderByCreatedAtDesc();
        return feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get average rating for a facility
     */
    public Double getAverageRatingByFacilityId(UUID facilityId) {
        log.info("Fetching average rating for facilityId: {}", facilityId);
        return feedbackRepository.getAverageRatingByFacilityId(facilityId);
    }

    /**
     * Get feedback count for a facility
     */
    public Long getFeedbackCountByFacilityId(UUID facilityId) {
        log.info("Fetching feedback count for facilityId: {}", facilityId);
        return feedbackRepository.countByFacilityId(facilityId);
    }

    /**
     * Map Feedback entity to FeedbackResponse DTO
     */
    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .recycleRequestId(feedback.getRecycleRequest().getId())
                .userId(feedback.getUserId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .category(feedback.getCategory())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }
}

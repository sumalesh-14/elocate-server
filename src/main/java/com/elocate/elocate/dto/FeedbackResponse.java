package com.elocate.elocate.dto;

import com.elocate.elocate.model.Feedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private UUID id;
    private UUID recycleRequestId;
    private UUID userId;
    private Integer rating;
    private String comment;
    private Feedback.FeedbackCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

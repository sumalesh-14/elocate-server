package com.elocate.elocate.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending reminder to intermediary/facility owner
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendReminderDto {
    
    /**
     * Optional comment/message from citizen to facility owner
     */
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String comment;
}

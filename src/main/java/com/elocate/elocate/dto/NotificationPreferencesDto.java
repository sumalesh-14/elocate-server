package com.elocate.elocate.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDto {
    private Boolean newRequests;
    private Boolean dailySummary;
    private Boolean weeklyReport;
    private Boolean marketing;
}

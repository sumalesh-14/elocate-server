package com.elocate.elocate.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Builder.Default
    @Column(name = "new_requests")
    private Boolean newRequests = true;

    @Builder.Default
    @Column(name = "daily_summary")
    private Boolean dailySummary = true;

    @Builder.Default
    @Column(name = "weekly_report")
    private Boolean weeklyReport = false;

    @Builder.Default
    @Column(name = "marketing")
    private Boolean marketing = false;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

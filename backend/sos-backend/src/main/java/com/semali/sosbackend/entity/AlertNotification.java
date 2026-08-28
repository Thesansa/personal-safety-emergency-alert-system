package com.semali.sosbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alert_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trusted_contact_id", nullable = false)
    private TrustedContact trustedContact;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "delivery_status", nullable = false)
    private String deliveryStatus;

    @Column(name = "notified_at", nullable = false)
    private LocalDateTime notifiedAt;
}
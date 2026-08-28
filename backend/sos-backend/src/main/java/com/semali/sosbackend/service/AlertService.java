package com.semali.sosbackend.service;

import com.semali.sosbackend.dto.AlertResponse;
import com.semali.sosbackend.dto.TriggerAlertRequest;
import com.semali.sosbackend.dto.LocationPingRequest;
import com.semali.sosbackend.dto.LocationResponse;
import com.semali.sosbackend.entity.*;
import com.semali.sosbackend.exception.ResourceNotFoundException;
import com.semali.sosbackend.exception.InvalidAlertStateException;
import com.semali.sosbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final AlertLocationRepository locationRepository;
    private final AlertNotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final TrustedContactRepository trustedContactRepository;

    @Transactional
    public AlertResponse trigger(UUID userId, TriggerAlertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Alert alert = Alert.builder()
                .user(user)
                .status(AlertStatus.ACTIVE)
                .triggeredAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        logStatusChange(alert, null, AlertStatus.ACTIVE, "USER", "SOS triggered by user");

        if (request != null && request.getLatitude() != null && request.getLongitude() != null) {
            saveLocationPing(alert, request.getLatitude(), request.getLongitude());
        }

        notifyTrustedContacts(alert, NotificationType.INITIAL);

        return mapToResponse(alert);
    }

    private void notifyTrustedContacts(Alert alert, String notificationType) {
        List<TrustedContact> contacts = trustedContactRepository.findByUserIdOrderByPriorityOrder(alert.getUser().getId());

        for (TrustedContact contact : contacts) {
            boolean sent = notificationService.sendAlert(contact, alert, notificationType);

            AlertNotification notification = AlertNotification.builder()
                    .alert(alert)
                    .trustedContact(contact)
                    .notificationType(notificationType)
                    .deliveryStatus(sent ? DeliveryStatus.SENT : DeliveryStatus.FAILED)
                    .notifiedAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }
    @Transactional
    public AlertResponse cancel(UUID id, UUID userId) {
        Alert alert = alertRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        String previousStatus = alert.getStatus();
        alert.setStatus(AlertStatus.CANCELLED);
        alert.setCancelledAt(LocalDateTime.now());
        alertRepository.save(alert);

        logStatusChange(alert, previousStatus, AlertStatus.CANCELLED, "USER", "Cancelled by user");

        return mapToResponse(alert);
    }

    @Transactional
    public AlertResponse resolve(UUID id, UUID userId) {
        Alert alert = alertRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        String previousStatus = alert.getStatus();
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy("USER");
        alertRepository.save(alert);

        logStatusChange(alert, previousStatus, AlertStatus.RESOLVED, "USER", "Resolved by user");

        return mapToResponse(alert);
    }

    @Transactional
    public List<AlertResponse> getAllForUser(UUID userId) {
        return alertRepository.findByUserIdOrderByTriggeredAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void logStatusChange(Alert alert, String previousStatus, String newStatus,
                                 String changedBy, String note) {
        AlertStatusHistory history = AlertStatusHistory.builder()
                .alert(alert)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .note(note)
                .build();

        historyRepository.save(history);
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .status(alert.getStatus())
                .triggeredAt(alert.getTriggeredAt())
                .escalatedAt(alert.getEscalatedAt())
                .resolvedAt(alert.getResolvedAt())
                .cancelledAt(alert.getCancelledAt())
                .build();
    }

    //location triggering

    @Transactional
    public void addLocationPing(UUID alertId, UUID userId, LocationPingRequest request) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!AlertStatus.ACTIVE.equals(alert.getStatus()) && !AlertStatus.ESCALATED.equals(alert.getStatus())) {
            throw new InvalidAlertStateException("Cannot add a location to an alert that isn't active or escalated");
        }

        saveLocationPing(alert, request.getLatitude(), request.getLongitude());
    }

    @Transactional
    public List<LocationResponse> getLocations(UUID alertId, UUID userId) {
        alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        return locationRepository.findByAlertIdOrderByCapturedAtAsc(alertId)
                .stream()
                .map(loc -> LocationResponse.builder()
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .capturedAt(loc.getCapturedAt())
                        .build())
                .toList();
    }

    private void saveLocationPing(Alert alert, Double latitude, Double longitude) {
        AlertLocation location = AlertLocation.builder()
                .alert(alert)
                .latitude(latitude)
                .longitude(longitude)
                .capturedAt(LocalDateTime.now())
                .build();

        locationRepository.save(location);
    }

    @Transactional
    public void escalate(UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!AlertStatus.ACTIVE.equals(alert.getStatus())) {
            return;
        }

        String previousStatus = alert.getStatus();
        alert.setStatus(AlertStatus.ESCALATED);
        alert.setEscalatedAt(LocalDateTime.now());
        alertRepository.save(alert);

        logStatusChange(alert, previousStatus, AlertStatus.ESCALATED, "SYSTEM", "No response within escalation window");
        notifyTrustedContacts(alert, NotificationType.ESCALATION);
    }



        }


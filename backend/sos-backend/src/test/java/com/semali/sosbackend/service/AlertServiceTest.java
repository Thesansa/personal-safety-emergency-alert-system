package com.semali.sosbackend.service;

import com.semali.sosbackend.dto.AlertResponse;
import com.semali.sosbackend.dto.TriggerAlertRequest;
import com.semali.sosbackend.entity.*;
import com.semali.sosbackend.exception.ResourceNotFoundException;
import com.semali.sosbackend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock private AlertRepository alertRepository;
    @Mock private AlertStatusHistoryRepository historyRepository;
    @Mock private UserRepository userRepository;
    @Mock private AlertLocationRepository locationRepository;
    @Mock private AlertNotificationRepository notificationRepository;
    @Mock private NotificationService notificationService;
    @Mock private TrustedContactRepository trustedContactRepository;

    @InjectMocks
    private AlertService alertService;

    // ---------- TRIGGER ----------

    @Test
    void trigger_shouldCreateActiveAlert_andNotifyContacts() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        TrustedContact contact = TrustedContact.builder()
                .id(UUID.randomUUID()).name("Mother").email("mother@example.com").build();

        TriggerAlertRequest request = new TriggerAlertRequest();
        request.setLatitude(6.9271);
        request.setLongitude(79.8612);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(trustedContactRepository.findByUserIdOrderByPriorityOrder(userId))
                .thenReturn(List.of(contact));
        when(notificationService.sendAlert(any(), any(), anyString())).thenReturn(true);

        AlertResponse response = alertService.trigger(userId, request);

        assertEquals(AlertStatus.ACTIVE, response.getStatus());
        verify(historyRepository).save(any(AlertStatusHistory.class));
        verify(locationRepository).save(any(AlertLocation.class));
        verify(notificationService).sendAlert(eq(contact), any(Alert.class), eq(NotificationType.INITIAL));
        verify(notificationRepository).save(any(AlertNotification.class));
    }

    @Test
    void trigger_shouldThrow_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.trigger(userId, new TriggerAlertRequest()));

        verify(alertRepository, never()).save(any());
    }

    // ---------- CANCEL / RESOLVE — ownership ----------

    @Test
    void cancel_shouldThrow_whenNotOwnedByUser() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(alertRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alertService.cancel(id, userId));
    }

    @Test
    void resolve_shouldSetStatusAndTimestamp_whenOwnedByUser() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Alert alert = Alert.builder().id(id).status(AlertStatus.ACTIVE).build();

        when(alertRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);

        AlertResponse response = alertService.resolve(id, userId);

        assertEquals(AlertStatus.RESOLVED, response.getStatus());
        assertNotNull(response.getResolvedAt());
        verify(historyRepository).save(any(AlertStatusHistory.class));
    }

    // ---------- ESCALATE ----------

    @Test
    void escalate_shouldChangeStatus_andNotify_whenAlertIsActive() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        Alert alert = Alert.builder().id(id).status(AlertStatus.ACTIVE).user(user).build();

        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenReturn(alert);
        when(trustedContactRepository.findByUserIdOrderByPriorityOrder(userId)).thenReturn(List.of());

        alertService.escalate(id);

        assertEquals(AlertStatus.ESCALATED, alert.getStatus());
        verify(historyRepository).save(any(AlertStatusHistory.class));
    }

    @Test
    void escalate_shouldDoNothing_whenAlertAlreadyResolved() {
        UUID id = UUID.randomUUID();
        Alert alert = Alert.builder().id(id).status(AlertStatus.RESOLVED).build();

        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));

        alertService.escalate(id);

        // Guards the race condition: an alert resolved between the scheduler's
        // query and this call must not be overwritten back to ESCALATED.
        assertEquals(AlertStatus.RESOLVED, alert.getStatus());
        verify(alertRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    // ---------- LOCATION ----------

    @Test
    void addLocationPing_shouldThrow_whenAlertIsResolved() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Alert alert = Alert.builder().id(id).status(AlertStatus.RESOLVED).build();

        when(alertRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(alert));

        assertThrows(
                com.semali.sosbackend.exception.InvalidAlertStateException.class,
                () -> alertService.addLocationPing(id, userId, new com.semali.sosbackend.dto.LocationPingRequest())
        );

        verify(locationRepository, never()).save(any());
    }

    @Test
    void addLocationPing_shouldSucceed_whenAlertIsActive() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Alert alert = Alert.builder().id(id).status(AlertStatus.ACTIVE).build();

        com.semali.sosbackend.dto.LocationPingRequest request =
                new com.semali.sosbackend.dto.LocationPingRequest();
        request.setLatitude(6.9);
        request.setLongitude(79.8);

        when(alertRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(alert));

        alertService.addLocationPing(id, userId, request);

        verify(locationRepository).save(any(AlertLocation.class));
    }
}
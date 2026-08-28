package com.semali.sosbackend.scheduler;

import com.semali.sosbackend.entity.Alert;
import com.semali.sosbackend.entity.AlertStatus;
import com.semali.sosbackend.repository.AlertRepository;
import com.semali.sosbackend.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EscalationScheduler {

    private final AlertRepository alertRepository;
    private final AlertService alertService;

    @Value("${alert.escalation-window-seconds}")
    private long escalationWindowSeconds;

    @Scheduled(fixedRate = 5000)
    public void checkForEscalations() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(escalationWindowSeconds);

        List<Alert> dueAlerts = alertRepository.findByStatus(AlertStatus.ACTIVE)
                .stream()
                .filter(alert -> alert.getTriggeredAt().isBefore(cutoff))
                .toList();

        for (Alert alert : dueAlerts) {
            alertService.escalate(alert.getId());
        }
    }
}
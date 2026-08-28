package com.semali.sosbackend.service;

import com.semali.sosbackend.entity.Alert;
import com.semali.sosbackend.entity.TrustedContact;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public boolean sendAlert(TrustedContact contact, Alert alert, String notificationType) {
        if (contact.getEmail() == null || contact.getEmail().isBlank()) {
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(contact.getEmail());
            message.setSubject(buildSubject(notificationType, alert));
            message.setText(buildBody(contact, alert));
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildSubject(String notificationType, Alert alert) {
        if ("ESCALATION".equals(notificationType)) {
            return "URGENT: Emergency Alert Escalated";
        }
        return "Emergency Alert Triggered";
    }

    private String buildBody(TrustedContact contact, Alert alert) {
        return String.format(
                "Hello %s,%n%nAn emergency alert has been triggered by someone who listed you as a trusted contact.%n%n"
                        + "Alert ID: %s%nTriggered at: %s%n%n"
                        + "This is a demo notification — live location tracking and a direct response link "
                        + "will be added in a future version.",
                contact.getName(), alert.getId(), alert.getTriggeredAt()
        );
    }
}
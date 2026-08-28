package com.semali.sosbackend.service;

import com.semali.sosbackend.entity.Alert;
import com.semali.sosbackend.entity.TrustedContact;

public interface NotificationService {
    boolean sendAlert(TrustedContact contact, Alert alert, String notificationType);
}
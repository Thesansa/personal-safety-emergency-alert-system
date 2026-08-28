package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.AlertNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertNotificationRepository extends JpaRepository<AlertNotification, UUID> {
}
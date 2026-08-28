package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

    Optional<Alert> findByIdAndUserId(UUID id, UUID userId);

    List<Alert> findByUserIdOrderByTriggeredAtDesc(UUID userId);

    List<Alert> findByStatus(String status);
}
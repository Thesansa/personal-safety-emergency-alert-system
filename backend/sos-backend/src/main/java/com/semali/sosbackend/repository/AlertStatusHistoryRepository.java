package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.AlertStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertStatusHistoryRepository extends JpaRepository<AlertStatusHistory, UUID> {

    List<AlertStatusHistory> findByAlertIdOrderByChangedAtAsc(UUID alertId);
}
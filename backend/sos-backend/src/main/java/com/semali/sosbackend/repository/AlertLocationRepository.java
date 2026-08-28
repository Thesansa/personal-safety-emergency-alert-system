package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.AlertLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertLocationRepository extends JpaRepository<AlertLocation, UUID> {

    List<AlertLocation> findByAlertIdOrderByCapturedAtAsc(UUID alertId);
}
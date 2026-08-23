package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrustedContactRepository extends JpaRepository<TrustedContact, UUID> {

    Optional<TrustedContact> findByIdAndUserId(UUID id, UUID userId);

    List<TrustedContact> findByUserIdOrderByPriorityOrder(UUID userId);
}
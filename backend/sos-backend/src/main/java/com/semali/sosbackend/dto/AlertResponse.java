package com.semali.sosbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AlertResponse {
    private UUID id;
    private String status;
    private LocalDateTime triggeredAt;
    private LocalDateTime escalatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime cancelledAt;
}
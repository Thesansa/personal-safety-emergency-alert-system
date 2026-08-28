package com.semali.sosbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LocationResponse {
    private Double latitude;
    private Double longitude;
    private LocalDateTime capturedAt;
}
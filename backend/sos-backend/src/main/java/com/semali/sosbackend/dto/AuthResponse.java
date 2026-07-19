package com.semali.sosbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private UUID userId;
    private String fullName;
    private String email;
    private String accessToken;
    private String refreshToken;
}
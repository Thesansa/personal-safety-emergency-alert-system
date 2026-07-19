package com.semali.sosbackend.service;

import com.semali.sosbackend.dto.AuthResponse;
import com.semali.sosbackend.dto.LoginRequest;
import com.semali.sosbackend.dto.RefreshRequest;
import com.semali.sosbackend.dto.RegisterRequest;
import com.semali.sosbackend.entity.RefreshToken;
import com.semali.sosbackend.entity.User;
import com.semali.sosbackend.exception.DuplicateResourceException;
import com.semali.sosbackend.exception.InvalidCredentialsException;
import com.semali.sosbackend.exception.InvalidTokenException;
import com.semali.sosbackend.repository.RefreshTokenRepository;
import com.semali.sosbackend.repository.UserRepository;
import com.semali.sosbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByNic(req.getNic())) {
            throw new DuplicateResourceException("NIC is already registered");
        }
        if (userRepository.existsByContactNo(req.getContactNo())) {
            throw new DuplicateResourceException("Contact number is already registered");
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .gender(req.getGender())
                .contactNo(req.getContactNo())
                .nic(req.getNic())
                .homeAddress(req.getHomeAddress())
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        String tokenHash = hashToken(req.getRefreshToken());

        RefreshToken existing = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        // Rotate: revoke the old token, issue a brand new one
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        refreshTokenRepository.deleteByUser(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId().toString(), user.getEmail());
        String rawRefreshToken = generateRawRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .build();
    }

    private String generateRawRefreshToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
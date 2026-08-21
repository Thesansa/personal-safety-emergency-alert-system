package com.semali.sosbackend.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Mirrors the @Value-injected field from application.properties
        ReflectionTestUtils.setField(
                authService,
                "refreshTokenExpirationMs",
                2592000000L
        );
    }

    // ---------- REGISTER ----------

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setNic("199912345678");
        request.setContactNo("0771234567");

        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldSucceed_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("new@example.com");
        request.setPassword("SecurePass123");
        request.setNic("199912345678");
        request.setContactNo("0771234567");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNic(anyString())).thenReturn(false);
        when(userRepository.existsByContactNo(anyString())).thenReturn(false);
        when(passwordEncoder.encode("SecurePass123"))
                .thenReturn("hashed_password");

        when(jwtUtil.generateAccessToken(anyString(), anyString()))
                .thenReturn("fake-access-token");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(UUID.randomUUID());
            return savedUser;
        });

        var response = authService.register(request);

        assertEquals("new@example.com", response.getEmail());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    // ---------- LOGIN ----------

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void login_shouldThrow_whenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("correct_hash")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(passwordEncoder.matches(
                "wrongPassword",
                "correct_hash"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }

    // ---------- REFRESH TOKEN ----------

    @Test
    void refresh_shouldReturnNewTokens_whenTokenIsValid() {

        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .build();

        RefreshToken existingToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("hashed-existing-token")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid-raw-token");

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(existingToken));

        when(jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getEmail()
        )).thenReturn("new-access-token");

        // Act
        var response = authService.refresh(request);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());

        // The old refresh token must be revoked
        assertTrue(existingToken.isRevoked());

        // Old token is saved after being revoked
        verify(refreshTokenRepository)
                .save(existingToken);

        // A new refresh token is also created and saved
        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void refresh_shouldThrow_whenTokenDoesNotExist() {

        // Arrange
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("unknown-token");

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                InvalidTokenException.class,
                () -> authService.refresh(request)
        );

        // No token should be saved
        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void refresh_shouldThrow_whenTokenIsRevoked() {

        // Arrange
        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .revoked(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(revokedToken));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-raw-token");

        // Act + Assert
        assertThrows(
                InvalidTokenException.class,
                () -> authService.refresh(request)
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void refresh_shouldThrow_whenTokenIsExpired() {

        // Arrange
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expiredToken));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-raw-token");

        // Act + Assert
        assertThrows(
                InvalidTokenException.class,
                () -> authService.refresh(request)
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void refresh_shouldRevokeOldTokenAndCreateNewToken_whenRotationOccurs() {

        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .fullName("Test User")
                .email("test@example.com")
                .build();

        RefreshToken oldToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("old-token-hash")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old-raw-token");

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(oldToken));

        when(jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getEmail()
        )).thenReturn("rotated-access-token");

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        // Act
        var response = authService.refresh(request);

        // Assert
        assertNotNull(response);
        assertEquals(
                "rotated-access-token",
                response.getAccessToken()
        );

        assertNotNull(response.getRefreshToken());

        // Old token is revoked
        assertTrue(oldToken.isRevoked());

        // Both the old and newly created tokens are persisted
        verify(refreshTokenRepository, times(2))
                .save(tokenCaptor.capture());

        var savedTokens = tokenCaptor.getAllValues();

        // First save = old token after revocation
        assertTrue(savedTokens.get(0).isRevoked());

        // Second save = newly generated refresh token
        assertFalse(savedTokens.get(1).isRevoked());

        assertNotNull(savedTokens.get(1).getTokenHash());
        assertNotNull(savedTokens.get(1).getExpiresAt());
    }

    // ---------- LOGOUT ----------

    @Test
    void logout_shouldDeleteRefreshTokens_forGivenUser() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        authService.logout(userId);

        verify(refreshTokenRepository)
                .deleteByUser(user);
    }
}
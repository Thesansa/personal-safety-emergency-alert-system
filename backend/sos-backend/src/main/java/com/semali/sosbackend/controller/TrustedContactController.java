package com.semali.sosbackend.controller;

import com.semali.sosbackend.dto.TrustedContactRequest;
import com.semali.sosbackend.dto.TrustedContactResponse;
import com.semali.sosbackend.service.TrustedContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trusted-contacts")
@RequiredArgsConstructor
public class TrustedContactController {

    private final TrustedContactService trustedContactService;

    @PostMapping
    public ResponseEntity<TrustedContactResponse> create(
            @Valid @RequestBody TrustedContactRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        TrustedContactResponse response = trustedContactService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TrustedContactResponse>> getAll(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<TrustedContactResponse> contacts = trustedContactService.getAllForUser(userId);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrustedContactResponse> getById(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        TrustedContactResponse response = trustedContactService.getById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrustedContactResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TrustedContactRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        TrustedContactResponse response = trustedContactService.update(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        trustedContactService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
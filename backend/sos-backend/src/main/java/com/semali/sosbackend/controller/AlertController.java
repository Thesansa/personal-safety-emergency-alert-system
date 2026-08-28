package com.semali.sosbackend.controller;

import com.semali.sosbackend.dto.AlertResponse;
import com.semali.sosbackend.dto.TriggerAlertRequest;
import com.semali.sosbackend.dto.LocationPingRequest;
import com.semali.sosbackend.dto.LocationResponse;
import com.semali.sosbackend.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/trigger")
    public ResponseEntity<AlertResponse> trigger(
            @RequestBody(required = false) TriggerAlertRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        AlertResponse response = alertService.trigger(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AlertResponse> cancel(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.cancel(id, userId));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolve(@PathVariable UUID id, Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.resolve(id, userId));
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAll(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.getAllForUser(userId));
    }

    //location trigger

    @PostMapping("/{id}/locations")
    public ResponseEntity<Void> addLocation(
            @PathVariable UUID id,
            @RequestBody LocationPingRequest request,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        alertService.addLocationPing(id, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationResponse>> getLocations(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.getLocations(id, userId));
    }
}
package com.semali.sosbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationPingRequest {
    private Double latitude;
    private Double longitude;
}
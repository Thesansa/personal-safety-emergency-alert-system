package com.semali.sosbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class TrustedContactResponse {

    private UUID id;
    private String name;
    private String contactNo;
    private String email;
    private String relation;
    private Integer priorityOrder;
}
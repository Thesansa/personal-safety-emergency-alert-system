package com.semali.sosbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(name = "full_name", nullable = false)
        private String fullName;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(name = "password_hash", nullable = false)
        private String passwordHash;

        private String gender;

        @Column(name = "contact_no", unique = true)
        private String contactNo;

        @Column(unique = true)
        private String nic;

        @Column(name = "home_address")
        private String homeAddress;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
        }
    }


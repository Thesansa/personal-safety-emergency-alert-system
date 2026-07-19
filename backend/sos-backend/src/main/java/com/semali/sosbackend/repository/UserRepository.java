package com.semali.sosbackend.repository;

import com.semali.sosbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNic(String nic);

    boolean existsByContactNo(String contactNo);
}
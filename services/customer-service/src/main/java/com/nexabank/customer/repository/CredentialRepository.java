package com.nexabank.customer.repository;

import com.nexabank.customer.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByEmailIgnoreCase(String email);
}


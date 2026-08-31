package com.nexabank.customer.service;

import com.nexabank.customer.dto.AuthResponse;
import com.nexabank.customer.dto.RegisterRequest;
import com.nexabank.customer.entity.Credential;
import com.nexabank.customer.entity.Customer;
import com.nexabank.customer.entity.Role;
import com.nexabank.customer.exception.ConflictException;
import com.nexabank.customer.repository.CredentialRepository;
import com.nexabank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock CredentialRepository credentialRepository;
    @Mock CustomerIdGenerator idGenerator;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @Test
    void registrationHashesPasswordAndIssuesCustomerToken() {
        RegisterRequest request = new RegisterRequest("Rohit", "Singh", "ROHIT@example.com",
                "9876543210", "securePassword");
        AuthResponse expected = new AuthResponse("signed-token", "Bearer", 3600);
        when(customerRepository.existsByEmailIgnoreCase("rohit@example.com")).thenReturn(false);
        when(idGenerator.nextId()).thenReturn("CUS-1001");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("securePassword")).thenReturn("bcrypt-hash");
        when(credentialRepository.save(any(Credential.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.issue(any(Credential.class))).thenReturn(expected);

        AuthService service = new AuthService(customerRepository, credentialRepository, idGenerator,
                passwordEncoder, jwtService);

        assertThat(service.register(request)).isEqualTo(expected);
        ArgumentCaptor<Credential> credential = ArgumentCaptor.forClass(Credential.class);
        verify(credentialRepository).save(credential.capture());
        assertThat(credential.getValue().getCustomerId()).isEqualTo("CUS-1001");
        assertThat(credential.getValue().getEmail()).isEqualTo("rohit@example.com");
        assertThat(credential.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(credential.getValue().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void registrationRejectsDuplicateEmailBeforeWriting() {
        RegisterRequest request = new RegisterRequest("Rohit", "Singh", "rohit@example.com",
                "9876543210", "securePassword");
        when(customerRepository.existsByEmailIgnoreCase("rohit@example.com")).thenReturn(true);
        AuthService service = new AuthService(customerRepository, credentialRepository, idGenerator,
                passwordEncoder, jwtService);

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(ConflictException.class);
        verify(customerRepository, never()).save(any());
        verify(credentialRepository, never()).save(any());
    }
}


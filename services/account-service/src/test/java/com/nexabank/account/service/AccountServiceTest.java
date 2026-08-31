package com.nexabank.account.service;

import com.nexabank.account.client.CustomerClient;
import com.nexabank.account.dto.AccountResponse;
import com.nexabank.account.dto.CreateAccountRequest;
import com.nexabank.account.entity.Account;
import com.nexabank.account.entity.AccountType;
import com.nexabank.account.exception.ConflictException;
import com.nexabank.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository repository;
    @Mock AccountIdGenerator idGenerator;
    @Mock CustomerClient customerClient;

    @Test
    void creationValidatesCustomerAndStartsAtZeroBalance() {
        when(repository.existsByCustomerIdAndAccountType("CUS-1001", AccountType.SAVINGS)).thenReturn(false);
        when(idGenerator.nextId()).thenReturn("ACC-10001");
        when(idGenerator.nextAccountNumber()).thenReturn("911234567890");
        when(repository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AccountService service = new AccountService(repository, idGenerator, customerClient);

        AccountResponse response = service.create("CUS-1001", "jwt-value",
                new CreateAccountRequest(AccountType.SAVINGS, "INR"));

        verify(customerClient).requireCustomer("CUS-1001", "jwt-value");
        assertThat(response.accountId()).isEqualTo("ACC-10001");
        assertThat(response.customerId()).isEqualTo("CUS-1001");
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(response.currency()).isEqualTo("INR");
    }

    @Test
    void duplicateAccountTypeIsRejected() {
        when(repository.existsByCustomerIdAndAccountType("CUS-1001", AccountType.CURRENT)).thenReturn(true);
        AccountService service = new AccountService(repository, idGenerator, customerClient);

        assertThatThrownBy(() -> service.create("CUS-1001", "jwt-value",
                new CreateAccountRequest(AccountType.CURRENT, "INR")))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void customerCannotReadAnotherCustomersAccount() {
        Account account = new Account("ACC-OTHER", "CUS-OTHER", "919999999999", AccountType.SAVINGS, "INR");
        when(repository.findById("ACC-OTHER")).thenReturn(Optional.of(account));
        AccountService service = new AccountService(repository, idGenerator, customerClient);

        assertThatThrownBy(() -> service.get("ACC-OTHER", "CUS-1001", false))
                .isInstanceOf(AccessDeniedException.class);
    }
}


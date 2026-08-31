package com.nexabank.account.service;

import com.nexabank.account.dto.TransferRequest;
import com.nexabank.account.entity.Account;
import com.nexabank.account.entity.AccountType;
import com.nexabank.account.exception.BusinessRuleException;
import com.nexabank.account.repository.AccountRepository;
import com.nexabank.account.repository.IdempotencyRecordRepository;
import com.nexabank.account.repository.LedgerEntryRepository;
import com.nexabank.account.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankingServiceTest {
    @Mock AccountRepository accounts;
    @Mock LedgerEntryRepository ledgerEntries;
    @Mock IdempotencyRecordRepository idempotencyRecords;
    @Mock OutboxEventRepository outboxEvents;
    @Mock IdempotencyCache idempotencyCache;

    private BankingService service;

    @BeforeEach
    void setUp() {
        service = new BankingService(accounts, ledgerEntries, idempotencyRecords, outboxEvents,
                new ObjectMapper(), idempotencyCache);
    }

    @Test
    void transferMovesBothBalancesAndPersistsLedgerResultAndEvent() {
        Account source = account("ACC-1", "CUS-1");
        Account destination = account("ACC-2", "CUS-2");
        source.deposit(new BigDecimal("100.00"));
        when(accounts.findByIdForUpdate("ACC-1")).thenReturn(Optional.of(source));
        when(accounts.findByIdForUpdate("ACC-2")).thenReturn(Optional.of(destination));

        var response = service.transfer(new TransferRequest("ACC-1", "ACC-2", new BigDecimal("35.50")),
                "CUS-1", false, "transfer-1", "request-1");

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(source.getBalance()).isEqualByComparingTo("64.50");
        assertThat(destination.getBalance()).isEqualByComparingTo("35.50");
        verify(ledgerEntries).saveAll(any());
        verify(idempotencyRecords).save(any());
        verify(outboxEvents).save(any());
    }

    @Test
    void insufficientFundsChangesNeitherAccountAndCreatesNoRecords() {
        Account source = account("ACC-1", "CUS-1");
        Account destination = account("ACC-2", "CUS-2");
        source.deposit(new BigDecimal("10.00"));
        when(accounts.findByIdForUpdate("ACC-1")).thenReturn(Optional.of(source));
        when(accounts.findByIdForUpdate("ACC-2")).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> service.transfer(
                new TransferRequest("ACC-1", "ACC-2", new BigDecimal("10.01")),
                "CUS-1", false, "transfer-2", "request-2"))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(source.getBalance()).isEqualByComparingTo("10.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("0.00");
    }

    private Account account(String id, String customerId) {
        return new Account(id, customerId, "91" + id.hashCode(), AccountType.SAVINGS, "INR");
    }
}

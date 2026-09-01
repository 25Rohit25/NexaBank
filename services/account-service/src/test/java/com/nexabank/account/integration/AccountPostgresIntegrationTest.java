package com.nexabank.account.integration;

import com.nexabank.account.entity.Account;
import com.nexabank.account.entity.AccountType;
import com.nexabank.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "spring.kafka.bootstrap-servers=localhost:1"
})
class AccountPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("pgvector/pgvector:0.8.6-pg17")
            .withDatabaseName("nexa_account")
            .withUsername("nexa")
            .withPassword("nexa-test-only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    AccountRepository accounts;

    @Test
    void flywaySchemaPersistsAndReadsAnAccount() {
        Account account = new Account("ACC-IT-1001", "CUS-IT-1001", "9000000000001001",
                AccountType.SAVINGS, "INR");

        accounts.saveAndFlush(account);

        assertThat(accounts.findById("ACC-IT-1001"))
                .get()
                .extracting(Account::getCustomerId, Account::getAccountType, Account::getCurrency)
                .containsExactly("CUS-IT-1001", AccountType.SAVINGS, "INR");
    }
}

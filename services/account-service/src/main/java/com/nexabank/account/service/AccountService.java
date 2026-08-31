package com.nexabank.account.service;

import com.nexabank.account.client.CustomerClient;
import com.nexabank.account.dto.AccountResponse;
import com.nexabank.account.dto.BalanceResponse;
import com.nexabank.account.dto.CreateAccountRequest;
import com.nexabank.account.entity.Account;
import com.nexabank.account.exception.ConflictException;
import com.nexabank.account.exception.ResourceNotFoundException;
import com.nexabank.account.repository.AccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AccountService {

    private final AccountRepository repository;
    private final AccountIdGenerator idGenerator;
    private final CustomerClient customerClient;

    public AccountService(AccountRepository repository, AccountIdGenerator idGenerator, CustomerClient customerClient) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.customerClient = customerClient;
    }

    @Transactional
    public AccountResponse create(String customerId, String bearerToken, CreateAccountRequest request) {
        customerClient.requireCustomer(customerId, bearerToken);
        if (repository.existsByCustomerIdAndAccountType(customerId, request.accountType())) {
            throw new ConflictException("Customer already has a " + request.accountType() + " account");
        }
        Account account = new Account(idGenerator.nextId(), customerId, idGenerator.nextAccountNumber(),
                request.accountType(), request.currency().toUpperCase(Locale.ROOT));
        return AccountResponse.from(repository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(String accountId, String customerId, boolean admin) {
        return AccountResponse.from(findAuthorized(accountId, customerId, admin));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getForCustomer(String requestedCustomerId, String actorId, boolean admin) {
        if (!admin && !requestedCustomerId.equals(actorId)) {
            throw new AccessDeniedException("Access denied");
        }
        return repository.findAllByCustomerIdOrderByCreatedAtAsc(requestedCustomerId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountId, String customerId, boolean admin) {
        Account account = findAuthorized(accountId, customerId, admin);
        return new BalanceResponse(account.getId(), account.getBalance(), account.getCurrency());
    }

    private Account findAuthorized(String accountId, String customerId, boolean admin) {
        Account account = repository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        if (!admin && !account.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("Access denied");
        }
        return account;
    }
}


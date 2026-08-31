package com.nexabank.account.service;

import com.nexabank.account.dto.DepositResponse;
import com.nexabank.account.dto.TransferRequest;
import com.nexabank.account.dto.TransferResponse;
import com.nexabank.account.entity.Account;
import com.nexabank.account.entity.IdempotencyRecord;
import com.nexabank.account.entity.LedgerEntry;
import com.nexabank.account.entity.LedgerEntryType;
import com.nexabank.account.entity.OutboxEvent;
import com.nexabank.account.exception.ConflictException;
import com.nexabank.account.exception.ResourceNotFoundException;
import com.nexabank.account.repository.AccountRepository;
import com.nexabank.account.repository.IdempotencyRecordRepository;
import com.nexabank.account.repository.LedgerEntryRepository;
import com.nexabank.account.repository.OutboxEventRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BankingService {
    static final String DEPOSIT_EVENT = "bank.transaction.completed";
    static final String TRANSFER_EVENT = "bank.transfer.completed";

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledgerEntries;
    private final IdempotencyRecordRepository idempotencyRecords;
    private final OutboxEventRepository outboxEvents;
    private final ObjectMapper objectMapper;
    private final IdempotencyCache idempotencyCache;

    public BankingService(AccountRepository accounts, LedgerEntryRepository ledgerEntries,
                          IdempotencyRecordRepository idempotencyRecords,
                          OutboxEventRepository outboxEvents, ObjectMapper objectMapper,
                          IdempotencyCache idempotencyCache) {
        this.accounts = accounts;
        this.ledgerEntries = ledgerEntries;
        this.idempotencyRecords = idempotencyRecords;
        this.outboxEvents = outboxEvents;
        this.objectMapper = objectMapper;
        this.idempotencyCache = idempotencyCache;
    }

    @Transactional
    public DepositResponse deposit(String accountId, BigDecimal requestedAmount, String actorId, boolean admin,
                                   String idempotencyKey, String correlationId) {
        String key = requireIdempotencyKey(idempotencyKey);
        BigDecimal amount = requestedAmount.setScale(2);
        String requestHash = hash("DEPOSIT|" + accountId + "|" + amount.toPlainString());
        DepositResponse replay = replay(actorId, key, requestHash, "DEPOSIT", DepositResponse.class);
        if (replay != null) return replay;

        Account account = lock(accountId);
        requireOwner(account, actorId, admin);
        replay = replay(actorId, key, requestHash, "DEPOSIT", DepositResponse.class);
        if (replay != null) return replay;

        Instant now = Instant.now();
        String transactionId = UUID.randomUUID().toString();
        account.deposit(amount);
        ledgerEntries.save(new LedgerEntry(transactionId, null, account.getId(), account.getCustomerId(),
                LedgerEntryType.DEPOSIT, amount, account.getCurrency(), now));
        DepositResponse response = new DepositResponse(transactionId, account.getId(), amount,
                account.getBalance(), account.getCurrency(), now);
        persistResult(actorId, key, requestHash, "DEPOSIT", response);
        persistEvent(transactionId, DEPOSIT_EVENT, Map.of(
                "eventId", UUID.randomUUID().toString(), "transactionId", transactionId,
                "accountId", account.getId(), "customerId", account.getCustomerId(),
                "amount", amount, "currency", account.getCurrency(), "occurredAt", now,
                "correlationId", correlationId));
        return response;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String actorId, boolean admin,
                                     String idempotencyKey, String correlationId) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new ConflictException("Source and destination accounts must differ");
        }
        String key = requireIdempotencyKey(idempotencyKey);
        BigDecimal amount = request.amount().setScale(2);
        String requestHash = hash("TRANSFER|" + request.sourceAccountId() + "|"
                + request.destinationAccountId() + "|" + amount.toPlainString());
        TransferResponse replay = replay(actorId, key, requestHash, "TRANSFER", TransferResponse.class);
        if (replay != null) return replay;

        List<String> lockOrder = List.of(request.sourceAccountId(), request.destinationAccountId()).stream()
                .sorted().toList();
        Account first = lock(lockOrder.get(0));
        Account second = lock(lockOrder.get(1));
        Account source = first.getId().equals(request.sourceAccountId()) ? first : second;
        Account destination = first == source ? second : first;
        requireOwner(source, actorId, admin);
        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new ConflictException("Cross-currency transfers are not supported");
        }
        replay = replay(actorId, key, requestHash, "TRANSFER", TransferResponse.class);
        if (replay != null) return replay;

        Instant now = Instant.now();
        String transferId = UUID.randomUUID().toString();
        String debitId = UUID.randomUUID().toString();
        String creditId = UUID.randomUUID().toString();
        source.debit(amount);
        destination.credit(amount);
        ledgerEntries.saveAll(List.of(
                new LedgerEntry(debitId, transferId, source.getId(), source.getCustomerId(),
                        LedgerEntryType.TRANSFER_DEBIT, amount, source.getCurrency(), now),
                new LedgerEntry(creditId, transferId, destination.getId(), destination.getCustomerId(),
                        LedgerEntryType.TRANSFER_CREDIT, amount, destination.getCurrency(), now)));
        TransferResponse response = new TransferResponse(transferId, debitId, creditId, source.getId(),
                destination.getId(), amount, source.getCurrency(), "COMPLETED", now);
        persistResult(actorId, key, requestHash, "TRANSFER", response);
        persistEvent(transferId, TRANSFER_EVENT, Map.ofEntries(
                Map.entry("eventId", UUID.randomUUID().toString()), Map.entry("transferId", transferId),
                Map.entry("debitTransactionId", debitId), Map.entry("creditTransactionId", creditId),
                Map.entry("sourceAccountId", source.getId()), Map.entry("sourceCustomerId", source.getCustomerId()),
                Map.entry("destinationAccountId", destination.getId()),
                Map.entry("destinationCustomerId", destination.getCustomerId()), Map.entry("amount", amount),
                Map.entry("currency", source.getCurrency()), Map.entry("occurredAt", now),
                Map.entry("correlationId", correlationId)));
        return response;
    }

    private Account lock(String accountId) {
        return accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    private void requireOwner(Account account, String actorId, boolean admin) {
        if (!admin && !account.getCustomerId().equals(actorId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private String requireIdempotencyKey(String key) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new IllegalArgumentException("Idempotency-Key must contain 1 to 128 characters");
        }
        return key.trim();
    }

    private <T> T replay(String actorId, String key, String requestHash, String operation, Class<T> type) {
        IdempotencyCache.CacheEntry cached = idempotencyCache.get(actorId, key);
        if (cached != null) {
            return deserializeReplay(cached.requestHash(), cached.operationType(), cached.responseJson(),
                    requestHash, operation, type);
        }
        return idempotencyRecords.findByActorIdAndIdempotencyKey(actorId, key).map(record -> {
            idempotencyCache.putAfterCommit(actorId, key, record.getRequestHash(),
                    record.getOperationType(), record.getResponseJson());
            return deserializeReplay(record.getRequestHash(), record.getOperationType(), record.getResponseJson(),
                    requestHash, operation, type);
        }).orElse(null);
    }

    private <T> T deserializeReplay(String storedHash, String storedOperation, String responseJson,
                                    String requestHash, String operation, Class<T> type) {
        if (!storedHash.equals(requestHash) || !storedOperation.equals(operation)) {
            throw new ConflictException("Idempotency-Key was already used for a different request");
        }
        try {
            return objectMapper.readValue(responseJson, type);
        } catch (Exception exception) {
            throw new IllegalStateException("Stored idempotency response is invalid", exception);
        }
    }

    private void persistResult(String actorId, String key, String requestHash, String operation, Object response) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            idempotencyRecords.save(new IdempotencyRecord(UUID.randomUUID().toString(), actorId, key,
                    requestHash, operation, responseJson, Instant.now()));
            idempotencyCache.putAfterCommit(actorId, key, requestHash, operation, responseJson);
        } catch (tools.jackson.core.JacksonException exception) {
            throw new IllegalStateException("Could not serialize operation response", exception);
        }
    }

    private void persistEvent(String aggregateId, String eventType, Object payload) {
        try {
            outboxEvents.save(new OutboxEvent(UUID.randomUUID().toString(), aggregateId, eventType,
                    objectMapper.writeValueAsString(payload), Instant.now()));
        } catch (tools.jackson.core.JacksonException exception) {
            throw new IllegalStateException("Could not serialize outbox event", exception);
        }
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}

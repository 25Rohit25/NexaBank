package com.nexabank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "idempotency_records", uniqueConstraints =
        @UniqueConstraint(name = "uq_idempotency_actor_key", columnNames = {"actor_id", "idempotency_key"}))
public class IdempotencyRecord {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "actor_id", length = 32, nullable = false, updatable = false)
    private String actorId;

    @Column(name = "idempotency_key", length = 128, nullable = false, updatable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64, nullable = false, updatable = false)
    private String requestHash;

    @Column(name = "operation_type", length = 24, nullable = false, updatable = false)
    private String operationType;

    @Column(name = "response_json", columnDefinition = "text", nullable = false, updatable = false)
    private String responseJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(String id, String actorId, String idempotencyKey, String requestHash,
                             String operationType, String responseJson, Instant createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.operationType = operationType;
        this.responseJson = responseJson;
        this.createdAt = createdAt;
    }

    public String getRequestHash() { return requestHash; }
    public String getOperationType() { return operationType; }
    public String getResponseJson() { return responseJson; }
}

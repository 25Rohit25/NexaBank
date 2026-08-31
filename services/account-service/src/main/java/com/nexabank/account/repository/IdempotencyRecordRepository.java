package com.nexabank.account.repository;

import com.nexabank.account.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
    Optional<IdempotencyRecord> findByActorIdAndIdempotencyKey(String actorId, String idempotencyKey);
}

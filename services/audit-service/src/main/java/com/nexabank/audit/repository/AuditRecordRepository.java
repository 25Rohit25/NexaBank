package com.nexabank.audit.repository;

import com.nexabank.audit.entity.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, String> {
}

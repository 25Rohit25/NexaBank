package com.nexabank.transaction.repository;

import com.nexabank.transaction.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, String>,
        JpaSpecificationExecutor<BankTransaction> {
    @Query("""
            select t from BankTransaction t
            where t.accountId = :accountId
              and (:fromTime is null or t.occurredAt >= :fromTime)
              and (:toTime is null or t.occurredAt <= :toTime)
              and (:minAmount is null or t.amount >= :minAmount)
            order by t.occurredAt desc
            """)
    List<BankTransaction> findAccountHistory(@Param("accountId") String accountId,
            @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime,
            @Param("minAmount") BigDecimal minAmount);
}

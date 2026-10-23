package com.example.intellibank.transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findTop20ByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}

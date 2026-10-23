package com.example.intellibank.transaction;
import java.math.BigDecimal;
import java.time.Instant;
public record TransactionDto(Long id, String description, String category, BigDecimal amount, String direction, Instant createdAt) {
    public static TransactionDto from(BankTransaction t) {
        return new TransactionDto(t.getId(), t.getDescription(), t.getCategory(), t.getAmount(), t.getDirection(), t.getCreatedAt());
    }
}

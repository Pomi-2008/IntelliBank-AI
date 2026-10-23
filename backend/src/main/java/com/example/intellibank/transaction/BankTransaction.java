package com.example.intellibank.transaction;

import com.example.intellibank.account.Account;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bank_transactions")
public class BankTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Account account;
    @Column(nullable = false, length = 120)
    private String description;
    @Column(nullable = false, length = 20)
    private String category;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 10)
    private String direction;
    @Column(nullable = false)
    private Instant createdAt;

    protected BankTransaction() {}
    public BankTransaction(Account account, String description, String category, BigDecimal amount, String direction) {
        this.account = account; this.description = description; this.category = category;
        this.amount = amount; this.direction = direction; this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public String getDirection() { return direction; }
    public Instant getCreatedAt() { return createdAt; }
}

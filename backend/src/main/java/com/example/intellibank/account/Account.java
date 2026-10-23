package com.example.intellibank.account;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank_accounts")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;
    @Column(nullable = false, length = 100)
    private String customerName;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    protected Account() {}
    public Account(String accountNumber, String customerName, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber; this.customerName = customerName;
        this.balance = balance; this.currency = currency;
    }
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getCustomerName() { return customerName; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}

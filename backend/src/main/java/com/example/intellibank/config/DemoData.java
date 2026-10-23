package com.example.intellibank.config;

import com.example.intellibank.account.Account;
import com.example.intellibank.account.AccountRepository;
import com.example.intellibank.transaction.BankTransaction;
import com.example.intellibank.transaction.TransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DemoData {
    @Bean CommandLineRunner seedDemoData(AccountRepository accounts, TransactionRepository transactions) {
        return args -> {
            if (accounts.count() > 0) return;
            Account primary = accounts.save(new Account("INDB0001001", "Aarav Sharma", new BigDecimal("84250.75"), "INR"));
            Account secondary = accounts.save(new Account("INDB0001002", "Priya Sharma", new BigDecimal("35600.00"), "INR"));
            transactions.saveAll(List.of(
                new BankTransaction(primary, "Monthly salary", "Salary", new BigDecimal("65000.00"), "CREDIT"),
                new BankTransaction(primary, "Grocery store", "Shopping", new BigDecimal("2450.50"), "DEBIT"),
                new BankTransaction(primary, "Electricity bill", "Bills", new BigDecimal("1820.00"), "DEBIT"),
                new BankTransaction(primary, "Online subscription", "Entertainment", new BigDecimal("499.00"), "DEBIT"),
                new BankTransaction(primary, "Cafe payment", "Food", new BigDecimal("620.00"), "DEBIT"),
                new BankTransaction(primary, "Freelance payment", "Other income", new BigDecimal("8500.00"), "CREDIT"),
                new BankTransaction(primary, "Fuel station", "Transport", new BigDecimal("2100.00"), "DEBIT"),
                new BankTransaction(primary, "Pharmacy", "Health", new BigDecimal("760.00"), "DEBIT"),
                new BankTransaction(secondary, "Monthly salary", "Salary", new BigDecimal("42000.00"), "CREDIT")
            ));
        };
    }
}

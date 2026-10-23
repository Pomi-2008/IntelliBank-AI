package com.example.intellibank.account;

import com.example.intellibank.transaction.TransactionDto;
import com.example.intellibank.transaction.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    public AccountController(AccountRepository accounts, TransactionRepository transactions) { this.accounts = accounts; this.transactions = transactions; }

    @GetMapping("/accounts")
    public List<Map<String, Object>> accounts() {
        return accounts.findAll().stream().map(a -> Map.<String,Object>of("id", a.getId(), "accountNumber", a.getAccountNumber(), "customerName", a.getCustomerName(), "balance", a.getBalance(), "currency", a.getCurrency())).toList();
    }

    @GetMapping("/dashboard/{accountId}")
    public Map<String, Object> dashboard(@PathVariable Long accountId) {
        Account a = accounts.findById(accountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        List<com.example.intellibank.transaction.BankTransaction> all = transactions.findByAccountIdOrderByCreatedAtDesc(accountId);
        BigDecimal income = all.stream().filter(t -> "CREDIT".equals(t.getDirection())).map(t -> t.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expenses = all.stream().filter(t -> "DEBIT".equals(t.getDirection())).map(t -> t.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("accountId", a.getId(), "customerName", a.getCustomerName(), "accountNumber", a.getAccountNumber(), "balance", a.getBalance(), "currency", a.getCurrency(), "totalIncome", income, "totalExpenses", expenses, "recentTransactions", all.stream().limit(8).map(TransactionDto::from).toList());
    }

    @GetMapping("/transactions/{accountId}")
    public List<TransactionDto> transactions(@PathVariable Long accountId) {
        if (!accounts.existsById(accountId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        return transactions.findTop20ByAccountIdOrderByCreatedAtDesc(accountId).stream().map(TransactionDto::from).toList();
    }
}

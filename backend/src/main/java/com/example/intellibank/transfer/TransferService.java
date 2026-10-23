package com.example.intellibank.transfer;

import com.example.intellibank.account.Account;
import com.example.intellibank.account.AccountRepository;
import com.example.intellibank.transaction.BankTransaction;
import com.example.intellibank.transaction.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class TransferService {
    private final AccountRepository accounts; private final TransactionRepository transactions;
    public TransferService(AccountRepository accounts, TransactionRepository transactions) { this.accounts = accounts; this.transactions = transactions; }

    @Transactional
    public Map<String, Object> transfer(TransferRequest request) {
        Account from = accounts.findById(request.fromAccountId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));
        Account to = accounts.findByAccountNumber(request.toAccountNumber()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient account not found"));
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        if (from.getId().equals(to.getId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot transfer to the same account");
        if (from.getBalance().compareTo(amount) < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        from.setBalance(from.getBalance().subtract(amount)); to.setBalance(to.getBalance().add(amount));
        accounts.save(from); accounts.save(to);
        transactions.save(new BankTransaction(from, "Transfer to " + to.getAccountNumber() + ": " + request.description().trim(), "Transfer", amount, "DEBIT"));
        transactions.save(new BankTransaction(to, "Transfer from " + from.getAccountNumber() + ": " + request.description().trim(), "Transfer", amount, "CREDIT"));
        return Map.of("status", "SUCCESS", "message", "Demo transfer completed", "fromAccountId", from.getId(), "toAccountNumber", to.getAccountNumber(), "amount", amount, "remainingBalance", from.getBalance());
    }
}

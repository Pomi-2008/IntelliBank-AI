package com.example.intellibank.ai;

import com.example.intellibank.account.Account;
import com.example.intellibank.account.AccountRepository;
import com.example.intellibank.transaction.BankTransaction;
import com.example.intellibank.transaction.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.*;

@Service
public class AssistantService {
    private final AccountRepository accounts; private final TransactionRepository transactions; private final RestClient ollama;
    @Value("${app.ai.model:llama3.2:3b}") private String model;
    public AssistantService(AccountRepository accounts, TransactionRepository transactions, @Value("${app.ai.ollama-url:http://localhost:11434}") String ollamaUrl) {
        this.accounts = accounts; this.transactions = transactions; this.ollama = RestClient.builder().baseUrl(ollamaUrl).build();
    }

    public Map<String,Object> answer(AssistantRequest request) {
        Long accountId = request.accountId() == null ? 1L : request.accountId();
        Account account = accounts.findById(accountId).orElse(null);
        if (account == null) return Map.of("answer", "I couldn't find that demo account. Refresh the dashboard and try again.", "mode", "fallback", "agent", "Supervisor");
        List<BankTransaction> txs = transactions.findByAccountIdOrderByCreatedAtDesc(accountId);
        BigDecimal spent = txs.stream().filter(t -> "DEBIT".equals(t.getDirection())).map(BankTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal received = txs.stream().filter(t -> "CREDIT".equals(t.getDirection())).map(BankTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String message = request.message().toLowerCase(Locale.ROOT);
        String agent = message.contains("loan") || message.contains("document") ? "Policy Agent" : (message.contains("spend") || message.contains("transaction") || message.contains("expense") || message.contains("income") ? "Transaction Analyst Agent" : "Supervisor");
        if (message.matches(".*\\b(transfer|send|move)\\s+(money|funds|₹|\\$|\\d).*" )
            || message.matches(".*\\b(execute|make|initiate)\\s+(a\\s+)?transfer\\b.*")) {
            return Map.of("answer", "I can't initiate or authorize transfers. Use the demo transfer form; the application validates the recipient and amount.", "mode", "fallback", "agent", "Policy Agent");
        }
        String context = "Demo account customer=" + account.getCustomerName() + ", balance=" + account.getCurrency() + " " + account.getBalance() + ", total recorded credits=" + received + ", total recorded debits=" + spent + ". Recent transactions: " + txs.stream().limit(10).map(t -> t.getCreatedAt() + " | " + t.getDirection() + " | " + t.getCategory() + " | " + t.getDescription() + " | " + t.getAmount()).toList();
        String prompt = "You are IntelliBank AI, a cautious assistant for a DEMO banking application. Answer clearly and briefly. Use only the supplied context for account-specific facts. Never claim to execute a transfer, approve a loan, or provide regulated financial advice. For loan document questions, say requirements vary by lender and suggest verifying with the bank.\nCONTEXT: " + context + "\nUSER: " + request.message();
        try {
            Map response = ollama.post().uri("/api/generate").contentType(MediaType.APPLICATION_JSON).body(Map.of("model", model, "prompt", prompt, "stream", false)).retrieve().body(Map.class);
            Object output = response == null ? null : response.get("response");
            if (output instanceof String text && !text.isBlank()) return Map.of("answer", text.trim(), "mode", "ollama", "agent", agent);
        } catch (Exception ignored) { /* Ollama is optional; keep the demo usable without a local model. */ }
        return Map.of("answer", fallback(message, account, spent, received), "mode", "fallback", "agent", agent, "note", "For generated answers, start Ollama locally; see README.");
    }

    private String fallback(String message, Account account, BigDecimal spent, BigDecimal received) {
        if (message.contains("balance")) return "Your demo account balance is " + account.getCurrency() + " " + account.getBalance() + ". This value comes from the application database.";
        if (message.contains("spend") || message.contains("expense") || message.contains("transaction")) return "Across the currently recorded demo transactions, debits total " + account.getCurrency() + " " + spent + " and credits total " + account.getCurrency() + " " + received + ". This is a summary of seeded/demo records, not a statement for a real bank account.";
        if (message.contains("loan") || message.contains("document")) return "Common loan applications may request identity proof, address proof, income or salary evidence, bank statements, and employment details. Exact requirements vary by lender and loan type; verify the current checklist with the bank. This assistant does not assess eligibility or approve loans.";
        return "Hello! I can summarize your demo balance, explain recorded transactions, compare credits and debits, or explain general banking concepts. Try asking: ‘How much have I spent?’ or ‘What documents are commonly needed for a personal loan?’";
    }
}

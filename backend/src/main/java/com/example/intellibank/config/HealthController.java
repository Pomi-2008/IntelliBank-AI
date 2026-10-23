package com.example.intellibank.config;

import com.example.intellibank.account.AccountRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final AccountRepository accounts;

    public HealthController(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public Map<String, Object> health() {
        long accountCount = accounts.count();
        return Map.of("status", "UP", "database", "UP", "accountCount", accountCount);
    }
}

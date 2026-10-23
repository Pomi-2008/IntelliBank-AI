package com.example.intellibank;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:api-tests;DB_CLOSE_DELAY=-1",
    "app.ai.ollama-url=http://localhost:1"
})
@AutoConfigureMockMvc
@Transactional
class BankingApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void listsAccountsDashboardAndTransactions() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.database").value("UP"));
        mvc.perform(get("/api/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].accountNumber").value("INDB0001001"))
            .andExpect(jsonPath("$[1].accountNumber").value("INDB0001002"));
        mvc.perform(get("/api/dashboard/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(84250.75))
            .andExpect(jsonPath("$.recentTransactions").isArray());
        mvc.perform(get("/api/transactions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(8));
        mvc.perform(get("/api/dashboard/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void successfulTransferUpdatesBothBalancesAndLedger() throws Exception {
        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
                .content(transfer(1, "INDB0001002", "500.25")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"));
        mvc.perform(get("/api/dashboard/1"))
            .andExpect(jsonPath("$.balance").value(83750.50));
        mvc.perform(get("/api/dashboard/2"))
            .andExpect(jsonPath("$.balance").value(36100.25));
        mvc.perform(get("/api/transactions/1"))
            .andExpect(jsonPath("$[0].direction").value("DEBIT"))
            .andExpect(jsonPath("$[0].amount").value(500.25));
        mvc.perform(get("/api/transactions/2"))
            .andExpect(jsonPath("$[0].direction").value("CREDIT"))
            .andExpect(jsonPath("$[0].amount").value(500.25));
    }

    @Test
    void rejectsInsufficientFundsInvalidAccountsNonPositiveAmountsAndSameAccount() throws Exception {
        assertRejected(transfer(1, "INDB0001002", "999999999"), 400, "Insufficient funds");
        assertRejected(transfer(1, "NO-SUCH-ACCOUNT", "1"), 404, "Recipient account not found");
        assertRejected(transfer(999, "INDB0001002", "1"), 404, "Source account not found");
        assertRejected(transfer(1, "INDB0001002", "0"), 400, "greater than zero");
        assertRejected(transfer(1, "INDB0001002", "-1"), 400, "greater than zero");
        assertRejected(transfer(1, "INDB0001002", "1.001"), 400, "two decimal places");
        assertRejected(transfer(1, "INDB0001001", "1"), 400, "same account");
        mvc.perform(get("/api/dashboard/1"))
            .andExpect(jsonPath("$.balance").value(84250.75));
    }

    @Test
    void assistantUsesFallbackWhenOllamaIsUnavailableAndCannotMoveMoney() throws Exception {
        mvc.perform(post("/api/assistant").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("accountId", 1, "message", "What is my balance?"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("fallback"))
            .andExpect(jsonPath("$.answer", containsString("84250.75")));
        mvc.perform(post("/api/assistant").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("accountId", 1, "message", "Please transfer money to the other account"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answer", containsString("can't initiate or authorize transfers")));
        mvc.perform(get("/api/dashboard/1"))
            .andExpect(jsonPath("$.balance").value(84250.75));
    }

    @Test
    void allowsFrontendCorsPreflight() throws Exception {
        mvc.perform(options("/api/accounts")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    private String transfer(long from, String to, String amount) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "fromAccountId", from,
            "toAccountNumber", to,
            "amount", new BigDecimal(amount),
            "description", "API test"
        ));
    }

    private void assertRejected(String body, int status, String message) throws Exception {
        mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().is(status))
            .andExpect(jsonPath("$.message", containsString(message)));
    }
}

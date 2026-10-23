package com.example.intellibank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "app.ai.ollama-url=http://localhost:11434"})
class IntelliBankApplicationTests { @Test void contextLoads() {} }

package com.example.intellibank.ai;
import jakarta.validation.constraints.NotBlank;
public record AssistantRequest(Long accountId, @NotBlank String message) {}

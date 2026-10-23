package com.example.intellibank.transfer;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record TransferRequest(@NotNull Long fromAccountId, @NotBlank String toAccountNumber, @NotNull @DecimalMin(value="0.01", message="Amount must be greater than zero") @Digits(integer=17, fraction=2, message="Amount must have at most two decimal places") BigDecimal amount, @NotBlank String description) {}

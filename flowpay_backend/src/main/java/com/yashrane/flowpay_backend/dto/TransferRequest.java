package com.yashrane.flowpay_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class TransferRequest {
    private Long fromWalletId;

    @NotNull(message = "Recipient wallet ID is required")
    private Long toWalletId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    public TransferRequest() {
    }

    public void setFromWalletId(Long fromWalletId) {
        this.fromWalletId = fromWalletId;
    }
    public Long getToWalletId() {
        return toWalletId;
    }
    public void setToWalletId(Long toWalletId) {
        this.toWalletId = toWalletId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
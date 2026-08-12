package com.yashrane.flowpay_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryResponse {
    private String type;       //DEBIT or CREDIT
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String counterpartyName;

    public LedgerEntryResponse(String type, BigDecimal amount, LocalDateTime createdAt, String counterpartyName) {
        this.type = type;
        this.amount = amount;
        this.createdAt = createdAt;
        this.counterpartyName = counterpartyName;
    }

    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCounterpartyName() { return counterpartyName; }
}

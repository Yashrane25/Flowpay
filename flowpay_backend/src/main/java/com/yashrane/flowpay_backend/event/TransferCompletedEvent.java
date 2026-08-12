package com.yashrane.flowpay_backend.event;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransferCompletedEvent implements Serializable {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;

    public TransferCompletedEvent() {}

    public TransferCompletedEvent(Long fromWalletId, Long toWalletId, BigDecimal amount) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amount = amount;
    }

    public Long getFromWalletId() { return fromWalletId; }
    public Long getToWalletId() { return toWalletId; }
    public BigDecimal getAmount() { return amount; }
}
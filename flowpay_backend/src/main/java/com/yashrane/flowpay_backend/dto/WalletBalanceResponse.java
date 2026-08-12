package com.yashrane.flowpay_backend.dto;

import java.math.BigDecimal;

public class WalletBalanceResponse {
    private Long walletId;
    private BigDecimal balance;

    public WalletBalanceResponse(Long walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public Long getWalletId() { return walletId; }
    public BigDecimal getBalance() { return balance; }
}
package com.yashrane.flowpay_backend.dto;

import java.math.BigDecimal;

public class TransferResponse {
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amountTransferred;
    private BigDecimal fromWalletNewBalance;
    private BigDecimal toWalletNewBalance;

    public TransferResponse(Long fromWalletId, Long toWalletId, BigDecimal amountTransferred,
                            BigDecimal fromWalletNewBalance, BigDecimal toWalletNewBalance) {
        this.fromWalletId = fromWalletId;
        this.toWalletId = toWalletId;
        this.amountTransferred = amountTransferred;
        this.fromWalletNewBalance = fromWalletNewBalance;
        this.toWalletNewBalance = toWalletNewBalance;
    }

    public Long getFromWalletId() { return fromWalletId; }
    public Long getToWalletId() { return toWalletId; }
    public BigDecimal getAmountTransferred() { return amountTransferred; }
    public BigDecimal getFromWalletNewBalance() { return fromWalletNewBalance; }
    public BigDecimal getToWalletNewBalance() { return toWalletNewBalance; }
}

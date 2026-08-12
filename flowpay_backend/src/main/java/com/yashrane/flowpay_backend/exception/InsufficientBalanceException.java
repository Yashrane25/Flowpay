package com.yashrane.flowpay_backend.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long walletId) {
        super("Wallet " + walletId + " has insufficient balance for this transfer");
    }
}

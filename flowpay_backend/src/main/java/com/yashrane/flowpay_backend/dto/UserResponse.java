package com.yashrane.flowpay_backend.dto;

import java.math.BigDecimal;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private BigDecimal walletBalance;

    public UserResponse(Long id, String fullName, String email, BigDecimal walletBalance) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.walletBalance = walletBalance;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }
}
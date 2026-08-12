package com.yashrane.flowpay_backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors; //null for nonValidation errors

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, Map<String, String> fieldErrors) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}

package com.yashrane.flowpay_backend.exception;

public class DuplicateRequestInFlightException extends RuntimeException {
    public DuplicateRequestInFlightException(String key) {
        super("A request with idempotency key " + key + " is already being processed");
    }
}

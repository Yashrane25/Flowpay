package com.yashrane.flowpay_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("api/health")
    public String healthCheck() {
        return "FlowPay backend is alive";
    }
}

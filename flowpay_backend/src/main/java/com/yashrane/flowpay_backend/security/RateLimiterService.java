package com.yashrane.flowpay_backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimiterService {
    @Value("${flowpay.ratelimit.capacity}")
    private int capacity;

    @Value("${flowpay.ratelimit.refill-tokens}")
    private int refillTokens;

    @Value("${flowpay.ratelimit.refill-duration-seconds}")
    private int refillDurationSeconds;

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String userKey) {
        Bucket bucket = buckets.computeIfAbsent(userKey, key -> newBucket());
        return bucket.tryConsume(1); //attempts to remove 1 token
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(capacity,
                io.github.bucket4j.Refill.greedy(refillTokens, Duration.ofSeconds(refillDurationSeconds)));
        return Bucket.builder().addLimit(limit).build();
    }
}

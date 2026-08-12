package com.yashrane.flowpay_backend.service;

import com.yashrane.flowpay_backend.entity.Wallet;
import com.yashrane.flowpay_backend.exception.WalletNotFoundException;
import com.yashrane.flowpay_backend.repository.WalletRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class WalletService {
    private static final String CACHE_KEY_PREFIX = "wallet:balance:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final WalletRepository walletRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public WalletService(WalletRepository walletRepository, RedisTemplate<String, String> redisTemplate){
        this.walletRepository = walletRepository;
        this.redisTemplate = redisTemplate;
    }

    public BigDecimal getBalance(Long walletId){
        String cacheKey = CACHE_KEY_PREFIX + walletId;

        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if(cachedValue != null){
            return new BigDecimal(cachedValue);
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        redisTemplate.opsForValue().set(cacheKey, wallet.getBalance().toString(), CACHE_TTL);
        return wallet.getBalance();
    }

    public void evictBalance(Long walletId) {
        redisTemplate.delete(CACHE_KEY_PREFIX + walletId);
    }
}
package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.service.VerificationCodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Validated
@Transactional
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final Map<String, String> codes = new ConcurrentHashMap<>();
    private final Map<String, Long> expiryTimes = new ConcurrentHashMap<>();
    private final Clock clock;

    public VerificationCodeServiceImpl() {
        this(Clock.systemDefaultZone());
    }

    public VerificationCodeServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String generateCode(String email) {
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        codes.put(email, code);
        expiryTimes.put(email, Instant.now(clock).toEpochMilli() + TimeUnit.MINUTES.toMillis(10));
        return code;
    }

    @Override
    public boolean validateCode(String email, String code) {
        if (!codes.containsKey(email)) {
            return false;
        }
        if (Instant.now(clock).toEpochMilli() > expiryTimes.get(email)) {
            codes.remove(email);
            expiryTimes.remove(email);
            return false;
        }
        return codes.get(email).equals(code);
    }
}

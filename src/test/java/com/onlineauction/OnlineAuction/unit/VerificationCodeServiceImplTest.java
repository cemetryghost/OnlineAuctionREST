package com.onlineauction.OnlineAuction.unit;

import com.onlineauction.OnlineAuction.service.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class VerificationCodeServiceImplTest {

    private VerificationCodeServiceImpl verificationCodeService;
    private Clock fixedClock;

    @BeforeEach
    public void setUp() {
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        verificationCodeService = new VerificationCodeServiceImpl(fixedClock);
    }

    @Test
    public void testGenerateAndValidateCode() {
        String email = "test@example.com";
        String code = verificationCodeService.generateCode(email);

        assertNotNull(code);
        assertTrue(verificationCodeService.validateCode(email, code));
    }

    @Test
    public void testValidateCode_Expired() {
        String email = "test@example.com";
        String code = verificationCodeService.generateCode(email);

        Clock expiredClock = Clock.offset(fixedClock, java.time.Duration.ofMinutes(11));
        VerificationCodeServiceImpl expiredVerificationCodeService = new VerificationCodeServiceImpl(expiredClock);

        assertFalse(expiredVerificationCodeService.validateCode(email, code));
    }

    @Test
    public void testValidateCode_Invalid() {
        String email = "test@example.com";
        verificationCodeService.generateCode(email);

        assertFalse(verificationCodeService.validateCode(email, "wrongCode"));
    }
}

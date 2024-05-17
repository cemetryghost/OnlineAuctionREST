package com.onlineauction.OnlineAuction.service;

public interface VerificationCodeService {
    String generateCode(String email);
    boolean validateCode(String email, String code);
}

package com.onlineauction.OnlineAuction.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}

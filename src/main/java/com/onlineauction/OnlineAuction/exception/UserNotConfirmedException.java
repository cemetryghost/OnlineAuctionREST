package com.onlineauction.OnlineAuction.exception;

import org.springframework.security.core.AuthenticationException;

public class UserNotConfirmedException extends AuthenticationException {
    public UserNotConfirmedException(String message) {
        super(message);
    }
}


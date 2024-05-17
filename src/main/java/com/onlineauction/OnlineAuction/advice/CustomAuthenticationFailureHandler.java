package com.onlineauction.OnlineAuction.advice;

import com.onlineauction.OnlineAuction.exception.UserNotConfirmedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.io.IOException;

@ControllerAdvice
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String redirectURL = "/auth/login?error=true";
        if (exception.getCause() instanceof LockedException) {
            redirectURL = "/auth/login?blocked=true";
        } else if (exception.getCause() instanceof UserNotConfirmedException) {
            redirectURL = "/auth/login?unconfirmed=true";
        }
        response.sendRedirect(redirectURL);
    }
}



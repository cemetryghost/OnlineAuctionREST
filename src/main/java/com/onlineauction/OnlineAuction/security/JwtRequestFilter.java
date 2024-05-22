package com.onlineauction.OnlineAuction.security;

import com.onlineauction.OnlineAuction.exception.JwtAuthenticationException;
import com.onlineauction.OnlineAuction.security.auth.JwtTokenService;
import com.onlineauction.OnlineAuction.service.impl.CustomUserDetailsServiceImpl;
import com.onlineauction.OnlineAuction.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsServiceImpl userDetailsService;
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        String accessToken = CookieUtil.extractTokenFromRequest(request, "accessToken");
        String refreshToken = CookieUtil.extractTokenFromRequest(request, "refreshToken");

        if (accessToken != null) {
            processAccessToken(accessToken, request, response, chain, refreshToken);
        } else if (refreshToken != null) {
            handleExpiredAccessToken(refreshToken, request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void processAccessToken(String accessToken, HttpServletRequest request, HttpServletResponse response, FilterChain chain, String refreshToken)
            throws IOException, ServletException {
        try {
            String username = jwtTokenService.extractUsername(accessToken);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateUser(username, request);
            }
        } catch (ExpiredJwtException e) {
            if (refreshToken != null) {
                handleExpiredAccessToken(refreshToken, request, response, chain);
                return;
            } else {
                throw new JwtAuthenticationException("Access токен истек");
            }
        } catch (Exception e) {
            throw new JwtAuthenticationException("Невалидный JWT токен", e);
        }
        chain.doFilter(request, response);
    }

    private void handleExpiredAccessToken(String refreshToken, HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            String username = jwtTokenService.extractUsername(refreshToken);
            if (username != null) {
                refreshAccessToken(username, refreshToken, request, response, chain);
            } else {
                throw new JwtAuthenticationException("Невалидный рефреш токен, логин пользователя null");
            }
        } catch (ExpiredJwtException ex) {
            throw new JwtAuthenticationException("Рефреш токен истек", ex);
        } catch (Exception ex) {
            throw new JwtAuthenticationException("Невалидный рефреш токен", ex);
        }
    }

    private void refreshAccessToken(String username, String refreshToken, HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (jwtTokenService.validateRefreshToken(refreshToken, userDetails)) {
            String newAccessToken = jwtTokenService.generateAccessToken(userDetails);
            CookieUtil.setCookie(response, "accessToken", newAccessToken, jwtTokenService.getAccessTokenExpirationInMs(), "Lax");
            setAuthenticationForUser(request, userDetails);

            request.setAttribute("NEW_ACCESS_TOKEN", newAccessToken);
            chain.doFilter(request, response);
        } else {
            throw new JwtAuthenticationException("Невалидный рефреш токен");
        }
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String accessToken = CookieUtil.extractTokenFromRequest(request, "accessToken");
        if (jwtTokenService.validateAccessToken(accessToken, userDetails)) {
            setAuthenticationForUser(request, userDetails);
        }
    }

    private void setAuthenticationForUser(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}

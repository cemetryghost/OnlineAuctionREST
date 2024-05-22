package com.onlineauction.OnlineAuction.security.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenService {
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    String extractUsername(String token);
    boolean validateAccessToken(String token, UserDetails userDetails);
    boolean validateRefreshToken(String token, UserDetails userDetails); // Add this method
    long getAccessTokenExpirationInMs();
    long getRefreshTokenExpirationInMs();
}

package com.onlineauction.OnlineAuction.service.impl;

import com.onlineauction.OnlineAuction.dto.AuthReqDTO;
import com.onlineauction.OnlineAuction.dto.AuthResDTO;
import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.security.auth.JwtTokenService;
import com.onlineauction.OnlineAuction.service.AuthService;
import com.onlineauction.OnlineAuction.service.UserService;
import com.onlineauction.OnlineAuction.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsServiceImpl customUserDetailsService;
    private final UserService userService;

    @Override
    public AuthResDTO authenticateUser(AuthReqDTO authenticationRequest, HttpServletResponse response) {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        CookieUtil.setCookie(response, "accessToken", accessToken, jwtTokenService.getAccessTokenExpirationInMs(), "Lax");
        CookieUtil.setCookie(response, "refreshToken", refreshToken, jwtTokenService.getRefreshTokenExpirationInMs(), "Lax");

        AuthResDTO responseBody = new AuthResDTO(accessToken);
        responseBody.setRole(userDetails.getAuthorities().iterator().next().getAuthority());
        responseBody.setRefreshToken(refreshToken);

        return responseBody;
    }


    @Override
    public AuthResDTO refreshToken(HttpServletRequest request, HttpServletResponse response) throws UserException {
        String refreshToken = CookieUtil.getCookieValue(request.getCookies(), "refreshToken");

        if (refreshToken == null) {
            throw new UserException("Рефреш токен не найден");
        }

        String username = jwtTokenService.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (jwtTokenService.validateRefreshToken(refreshToken, userDetails)) {
            String newAccessToken = jwtTokenService.generateAccessToken(userDetails);

            CookieUtil.setCookie(response, "accessToken", newAccessToken, jwtTokenService.getAccessTokenExpirationInMs(), "Lax");

            AuthResDTO responseBody = new AuthResDTO(newAccessToken);
            responseBody.setRole(userDetails.getAuthorities().iterator().next().getAuthority());
            responseBody.setRefreshToken(refreshToken);

            return responseBody;
        } else {
            throw new UserException("Невалидный рефреш токен");
        }
    }

    @Override
    public Map<String, String> registerUser(UserDTO userDTO) throws UserException {
        return userService.register(userDTO);
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}

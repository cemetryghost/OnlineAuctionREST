package com.onlineauction.OnlineAuction.controller;

import com.onlineauction.OnlineAuction.dto.AuthenticationDTO;
import com.onlineauction.OnlineAuction.dto.RegistrationDTO;
import com.onlineauction.OnlineAuction.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationDTO registrationDTO) {
        authService.register(registrationDTO);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO authenticationDTO) {
        String message = authService.authenticate(authenticationDTO);
        return ResponseEntity.ok(message);
    }
}

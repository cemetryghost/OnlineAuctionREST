package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.AuthReqDTO;
import com.onlineauction.OnlineAuction.dto.AuthResDTO;
import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.exception.UserException;
import com.onlineauction.OnlineAuction.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResDTO> login(@RequestBody @Valid AuthReqDTO authReqDTO, HttpServletResponse response) throws Exception {
        AuthResDTO authResDTO = authService.authenticateUser(authReqDTO, response);
        return ResponseEntity.ok(authResDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            AuthResDTO authResponse = authService.refreshToken(request, response);
            return ResponseEntity.ok(authResponse);
        } catch (UserException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            Map<String, String> response = authService.registerUser(userDTO);
            return ResponseEntity.ok(response);
        } catch (UserException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}

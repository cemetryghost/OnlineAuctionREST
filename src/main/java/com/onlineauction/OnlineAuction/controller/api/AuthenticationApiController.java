package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/auth")
@Validated
public class AuthenticationApiController {

    private final UserService authenticationService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        try {
            authenticationService.registerNewUser(userDTO);
            return ResponseEntity.ok(Map.of("message", "Пользователь зарегистрирован успешно"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

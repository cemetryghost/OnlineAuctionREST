package com.onlineauction.OnlineAuction.controller;

import com.onlineauction.OnlineAuction.dto.AuthenticationDTO;
import com.onlineauction.OnlineAuction.dto.RegistrationDTO;
import com.onlineauction.OnlineAuction.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "registration";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password"); // Добавляем сообщение об ошибке
        }
        return "auth";
    }

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationDTO registrationDTO, RedirectAttributes redirectAttributes) {
        authService.registerNewUserAccount(registrationDTO);
        redirectAttributes.addFlashAttribute("message", "User registered successfully");
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @Operation(summary = "Auth User")
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO authenticationDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid input data");
        }
        boolean authenticated = authService.authenticate(authenticationDTO);
        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid username or password"));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(Map.of("message", "User authenticated successfully"));
    }
}

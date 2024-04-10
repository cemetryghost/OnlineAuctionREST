package com.onlineauction.OnlineAuction.controller.api;

import com.onlineauction.OnlineAuction.dto.UserDTO;
import com.onlineauction.OnlineAuction.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/auth")
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

    @Operation(summary = "Register new user")
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO, RedirectAttributes redirectAttributes) {
        authenticationService.registerNewUser(userDTO);
        redirectAttributes.addFlashAttribute("message", "User registered successfully");
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

}

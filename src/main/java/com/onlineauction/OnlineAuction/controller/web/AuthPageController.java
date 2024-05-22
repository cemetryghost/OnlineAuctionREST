package com.onlineauction.OnlineAuction.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthPageController {

    @GetMapping("/login")
    public String showLoginForm() {
        return "/page_auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "/page_auth/registration";
    }
}

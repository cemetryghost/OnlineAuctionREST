package com.onlineauction.OnlineAuction.controller.web;

import org.springframework.ui.Model;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class BuyerWebController {

    @GetMapping("/buyer_dashboard")
    public String buyerDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "buyer_dashboard";
    }

    @GetMapping("/buyer_lots")
    public String buyerLots(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "buyer_lots";
    }

    @GetMapping("/my_bids")
    public String myBids(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "my_bids";
    }
}


package com.onlineauction.OnlineAuction.controller.web;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class SellerWebController {

    @GetMapping("/seller_dashboard")
    public String sellerDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/dashboard/seller_dashboard";
    }

    @GetMapping("/completed_lots")
    public String completedLots(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/page_seller/completed_lots";
    }
}

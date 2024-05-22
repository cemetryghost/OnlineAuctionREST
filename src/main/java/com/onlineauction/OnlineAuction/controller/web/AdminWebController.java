package com.onlineauction.OnlineAuction.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AdminWebController {

    @GetMapping("/admin_dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/dashboard/admin_dashboard";
    }

    @GetMapping("/categories")
    public String categoriesDetail(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/page_admin/categories";
    }

    @GetMapping("/products")
    public String productsDetail(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/page_seller/products";
    }

    @GetMapping("/accounts")
    public String accountsDetails(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "/page_admin/accounts";
    }
}

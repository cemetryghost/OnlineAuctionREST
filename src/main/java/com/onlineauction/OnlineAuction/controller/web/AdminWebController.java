package com.onlineauction.OnlineAuction.controller.web;

import com.onlineauction.OnlineAuction.dto.CategoryDTO;
import com.onlineauction.OnlineAuction.service.CategoryService;
import com.onlineauction.OnlineAuction.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.onlineauction.OnlineAuction.service.LotService;

import java.util.List;

@Controller
public class AdminWebController {

    private final LotService lotService;
    private final UserService userService;
    private final CategoryService categoryService;

    public AdminWebController(LotService lotService, UserService userService, CategoryService categoryService) {
        this.lotService = lotService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/admin_dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("role", authentication.getAuthorities().iterator().next().getAuthority());
            model.addAttribute("login", authentication.getName());
        }
        return "admin_dashboard";
    }

    @GetMapping("/categories")
    public String getAllCategories(Model model) {
        List<CategoryDTO> categoryDTOList = categoryService.getAllCategories();
        model.addAttribute("categories", categoryDTOList);
        return "categories";
    }
}




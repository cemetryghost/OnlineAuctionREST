package com.onlineauction.OnlineAuction.controller.web;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/buyer")
@AllArgsConstructor
public class BuyerWebController {

    @GetMapping
    public String buyerDashboard() {
        return "buyer_dashboard";
    }

//    TODO: Сделать продавца! Срочно!
}


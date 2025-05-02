package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.User;
import com.resort.managementsystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {
    private final UserRepository users;

    public AccountController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/account")
    public String account(Model model, Authentication auth) {
        String username = auth.getName();
        User user = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        model.addAttribute("user", user);
        return "account";
    }
}
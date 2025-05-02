package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Role;
import com.resort.managementsystem.entity.User;
import com.resort.managementsystem.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model m) {
        m.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, @RequestParam String role) {
        if (users.findByUsername(user.getUsername()).isPresent()) {
            return "redirect:/register?error";
        }
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRoles(Set.of(Role.valueOf("ROLE_"+role.toUpperCase())));
        users.save(user);
        return "redirect:/login?registered";
    }
}

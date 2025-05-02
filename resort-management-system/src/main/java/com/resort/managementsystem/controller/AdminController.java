
// src/main/java/com/resort/managementsystem/controller/AdminUserController.java
package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Role;
import com.resort.managementsystem.entity.User;
import com.resort.managementsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin/create-user";
    }

    @PostMapping("/create")
    public String createUser(
            @ModelAttribute User user,
            @RequestParam(value = "selectedRoles", required = false) List<String> selectedRoles
    ) {
        Set<Role> roles = new HashSet<>();
        if (selectedRoles != null) {
            roles = selectedRoles.stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());
        } else {
            roles.add(Role.ROLE_STAFF);
        }
        user.setRoles(roles);
        userRepository.save(user);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin/edit-user";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute User user,
            @RequestParam(value = "selectedRoles", required = false) List<String> selectedRoles
    ) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + id));
        existing.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(user.getPassword());
        }
        Set<Role> roles = new HashSet<>();
        if (selectedRoles != null) {
            roles = selectedRoles.stream().map(Role::valueOf).collect(Collectors.toSet());
        }
        existing.setRoles(roles);
        userRepository.save(existing);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users?deleted";
    }
}

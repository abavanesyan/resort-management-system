package com.resort.managementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SettingsController {

    // In-memory storage for settings (replace with a proper database in production)
    private Double baseRoomPrice = 100.0;
    private Double taxRate = 10.0;
    private Map<String, Double> loyaltyTiers = new HashMap<>();

    public SettingsController() {
        // Initialize default loyalty tiers
        loyaltyTiers.put("GOLD", 15.0);
        loyaltyTiers.put("SILVER", 10.0);
        loyaltyTiers.put("BRONZE", 5.0);
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("baseRoomPrice", baseRoomPrice);
        settings.put("taxRate", taxRate);
        settings.put("loyaltyTiers", loyaltyTiers);
        model.addAttribute("settings", settings);
        return "settings";
    }

    @PostMapping("/settings")
    public String saveSettings(
            @RequestParam("baseRoomPrice") Double baseRoomPrice,
            @RequestParam("taxRate") Double taxRate,
            @RequestParam("tierNames") List<String> tierNames,
            @RequestParam("discountPercentages") List<Double> discountPercentages,
            Model model) {
        try {
            this.baseRoomPrice = baseRoomPrice;
            this.taxRate = taxRate;
            Map<String, Double> newLoyaltyTiers = new HashMap<>();
            for (int i = 0; i < tierNames.size(); i++) {
                if (tierNames.get(i) != null && !tierNames.get(i).trim().isEmpty()) {
                    newLoyaltyTiers.put(tierNames.get(i).toUpperCase(), discountPercentages.get(i));
                }
            }
            this.loyaltyTiers = newLoyaltyTiers;
            return "redirect:/settings?success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save settings: " + e.getMessage());
            Map<String, Object> settings = new HashMap<>();
            settings.put("baseRoomPrice", baseRoomPrice);
            settings.put("taxRate", taxRate);
            settings.put("loyaltyTiers", loyaltyTiers);
            model.addAttribute("settings", settings);
            return "settings";
        }
    }
}
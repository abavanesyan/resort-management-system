package com.resort.managementsystem.entity;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double baseRoomPrice;

    @Column(nullable = false)
    private Double taxRate;

    @ElementCollection
    @CollectionTable(name = "loyalty_tiers", joinColumns = @JoinColumn(name = "settings_id"))
    @MapKeyColumn(name = "tier_name")
    @Column(name = "discount_percentage")
    private Map<String, Double> loyaltyTiers;

    public Settings() {
        this.baseRoomPrice = 100.0; // Default value
        this.taxRate = 10.0; // Default value (10%)
        this.loyaltyTiers = Map.of(
                "BRONZE", 5.0,
                "SILVER", 10.0,
                "GOLD", 15.0
        ); // Default tiers
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getBaseRoomPrice() {
        return baseRoomPrice;
    }

    public void setBaseRoomPrice(Double baseRoomPrice) {
        this.baseRoomPrice = baseRoomPrice;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public Map<String, Double> getLoyaltyTiers() {
        return loyaltyTiers;
    }

    public void setLoyaltyTiers(Map<String, Double> loyaltyTiers) {
        this.loyaltyTiers = loyaltyTiers;
    }
}
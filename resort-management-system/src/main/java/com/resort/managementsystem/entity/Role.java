package com.resort.managementsystem.entity;

public enum Role {
    ROLE_ADMIN,
    ROLE_STAFF;

    public String getName() {
        return name();
    }
}
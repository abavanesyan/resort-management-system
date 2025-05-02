package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
}
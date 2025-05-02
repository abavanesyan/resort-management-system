package com.resort.managementsystem.scheduler;

import com.resort.managementsystem.service.MaintenanceScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RoomStatusScheduler {

    @Autowired
    private MaintenanceScheduleService maintenanceScheduleService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateRoomStatuses() {
        maintenanceScheduleService.updateAllRoomStatuses();
    }
}
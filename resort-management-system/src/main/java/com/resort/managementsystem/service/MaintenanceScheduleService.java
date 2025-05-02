package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.MaintenanceSchedule;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.repository.MaintenanceScheduleRepository;
import com.resort.managementsystem.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceScheduleService {

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<MaintenanceSchedule> getAllSchedules() {
        return maintenanceScheduleRepository.findAll();
    }

    public Optional<MaintenanceSchedule> getScheduleById(Long id) {
        return maintenanceScheduleRepository.findById(id);
    }

    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        // Validate end date is after start date
        if (schedule.getEndDate().isBefore(schedule.getStartDate()) ||
                schedule.getEndDate().isEqual(schedule.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Check for overlapping maintenance schedules for the same room
        List<MaintenanceSchedule> overlappingSchedules = maintenanceScheduleRepository.findOverlappingSchedules(
                schedule.getRoom(),
                schedule.getStartDate(),
                schedule.getEndDate()
        );

        // Exclude the current schedule if it’s being updated
        overlappingSchedules.removeIf(s -> s.getId() != null && s.getId().equals(schedule.getId()));

        if (!overlappingSchedules.isEmpty()) {
            throw new IllegalArgumentException("The room is already scheduled for maintenance during the selected dates");
        }

        MaintenanceSchedule savedSchedule = maintenanceScheduleRepository.save(schedule);

        // Update room status if the schedule is active today
        updateRoomStatusForSchedule(savedSchedule.getRoom());

        return savedSchedule;
    }

    public void deleteSchedule(Long id) {
        MaintenanceSchedule schedule = maintenanceScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid maintenance schedule ID: " + id));
        Room room = schedule.getRoom();
        maintenanceScheduleRepository.deleteById(id);

        // Update room status after deletion
        updateRoomStatusForSchedule(room);
    }

    public void updateAllRoomStatuses() {
        List<Room> allRooms = roomRepository.findAll();
        for (Room room : allRooms) {
            updateRoomStatusForSchedule(room);
        }
    }

    private void updateRoomStatusForSchedule(Room room) {
        LocalDate today = LocalDate.now();
        List<MaintenanceSchedule> activeSchedules = maintenanceScheduleRepository.findActiveSchedulesOnDate(today);

        boolean isUnderMaintenance = activeSchedules.stream()
                .anyMatch(schedule -> schedule.getRoom().getId().equals(room.getId()));

        // Only update the status if it's not currently occupied (to avoid conflicts with reservations)
        if (!"Occupied".equals(room.getStatus())) {
            if (isUnderMaintenance) {
                room.setStatus("Under Maintenance");
            } else {
                room.setStatus("Available");
            }
            roomRepository.save(room);
        }
    }
}
package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.MaintenanceSchedule;
import com.resort.managementsystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.room = :room AND " +
            "((ms.startDate <= :endDate AND ms.endDate >= :startDate))")
    List<MaintenanceSchedule> findOverlappingSchedules(
            @Param("room") Room room,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE " +
            "ms.startDate <= :date AND ms.endDate >= :date")
    List<MaintenanceSchedule> findActiveSchedulesOnDate(@Param("date") LocalDate date);
}
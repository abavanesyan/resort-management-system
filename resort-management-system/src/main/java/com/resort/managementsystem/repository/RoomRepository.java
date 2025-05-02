package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE " +
            "(:roomType IS NULL OR r.roomType = :roomType) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:capacity IS NULL OR r.capacity = :capacity) AND " +
            "(:smokingPolicy IS NULL OR r.smokingPolicy = :smokingPolicy) AND " +
            "(:viewType IS NULL OR r.viewType = :viewType) AND " +
            "(:floorNumber IS NULL OR r.floorNumber = :floorNumber)")
    List<Room> findRoomsByCriteria(
            @Param("roomType") String roomType,
            @Param("status") String status,
            @Param("capacity") Integer capacity,
            @Param("smokingPolicy") String smokingPolicy,
            @Param("viewType") String viewType,
            @Param("floorNumber") Integer floorNumber
    );

    long countByStatus(String status);
}
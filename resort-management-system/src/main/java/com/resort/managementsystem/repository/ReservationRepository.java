package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.entity.Reservation;
import com.resort.managementsystem.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r " +
            "WHERE (:guestName IS NULL OR r.guest.name LIKE %:guestName%) " +
            "AND (:roomNumber IS NULL OR r.room.number LIKE %:roomNumber%) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR " +
            "(r.checkInDate <= :endDate AND r.checkOutDate >= :startDate))")
    List<Reservation> findReservationsByCriteria(
            @Param("guestName") String guestName,
            @Param("roomNumber") String roomNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.room = :room " +
            "AND r.checkInDate <= :endDate " +
            "AND r.checkOutDate >= :startDate")
    List<Reservation> findOverlappingReservations(
            @Param("room") Room room,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<Reservation> findByGuestOrderByCheckOutDateDesc(Guest guest);
}


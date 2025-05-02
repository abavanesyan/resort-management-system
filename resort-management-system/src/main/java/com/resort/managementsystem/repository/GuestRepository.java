package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    @Query("SELECT g FROM Guest g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(g.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Guest> searchGuests(String query);
}
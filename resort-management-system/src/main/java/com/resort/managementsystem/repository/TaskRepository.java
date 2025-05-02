package com.resort.managementsystem.repository;

import com.resort.managementsystem.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t JOIN FETCH t.assignedStaff WHERE t.assignedStaff.id = :staffId")
    List<Task> findByAssignedStaffId(Long staffId);

    void deleteByAssignedStaffId(Long staffId);

    @Query("SELECT t FROM Task t JOIN FETCH t.assignedStaff")
    List<Task> findAllWithAssignedStaff();

    @Query("SELECT t FROM Task t JOIN FETCH t.assignedStaff WHERE t.id = :id")
    Optional<Task> findByIdWithAssignedStaff(Long id);
}
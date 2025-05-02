package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.entity.Task;
import com.resort.managementsystem.repository.StaffRepository;
import com.resort.managementsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StaffRepository staffRepository;

    public Task createTask(Task task, Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));
        task.setAssignedStaff(staff);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAllWithAssignedStaff();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findByIdWithAssignedStaff(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public List<Task> getTasksByStaffId(Long staffId) {
        return taskRepository.findByAssignedStaffId(staffId);
    }

    public List<Task> filterTasks(String status, Long staffId, LocalDateTime dueDate) {
        List<Task> tasks = taskRepository.findAllWithAssignedStaff();
        if (status != null && !status.isEmpty()) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        if (staffId != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getAssignedStaff().getId().equals(staffId))
                    .collect(Collectors.toList());
        }
        if (dueDate != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getDueDate().toLocalDate().equals(dueDate.toLocalDate()))
                    .collect(Collectors.toList());
        }
        return tasks;
    }

    public Task updateTask(Long id, Task task) {
        Task existingTask = getTaskById(id);
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setAssignedStaff(task.getAssignedStaff());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
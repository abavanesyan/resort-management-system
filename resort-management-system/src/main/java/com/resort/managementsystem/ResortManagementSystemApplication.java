package com.resort.managementsystem;

import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.entity.Task;
import com.resort.managementsystem.repository.StaffRepository;
import com.resort.managementsystem.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class ResortManagementSystemApplication {

    private static final Logger logger = LoggerFactory.getLogger(ResortManagementSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ResortManagementSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner demoData(StaffRepository staffRepository, TaskRepository taskRepository) {
        return args -> {
            try {
                // Check if the staff already exists to avoid duplicate email error
                if (!staffRepository.findByEmail("john.doe@resort.com").isPresent()) {
                    Staff staff = new Staff("John", "Doe", "john.doe@resort.com", "manager");
                    staffRepository.save(staff);
                    logger.info("Created staff: John Doe");
                } else {
                    logger.info("Staff with email john.doe@resort.com already exists, skipping creation.");
                }

                // Retrieve the staff member by email
                Staff savedStaff = staffRepository.findByEmail("john.doe@resort.com")
                        .orElseThrow(() -> new RuntimeException("Staff not found"));

                // Create and save a task
                Task task = new Task("Clean Room 101", "pending", LocalDateTime.now().plusDays(1), savedStaff);
                taskRepository.save(task);
                logger.info("Created task: Clean Room 101");

                // Retrieve the staff member by ID
                Staff staffForTask = staffRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("Staff not found with ID 1"));

                Task anotherTask = new Task("Prepare Room 102", "in_progress", LocalDateTime.now().plusDays(2), staffForTask);
                taskRepository.save(anotherTask);
                logger.info("Created task: Prepare Room 102");
            } catch (Exception e) {
                logger.error("Error in demoData: ", e);
                throw e; // Re-throw to ensure the test fails and we can see the error
            }
        };
    }
}
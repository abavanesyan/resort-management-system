package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Reservation;
import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.repository.StaffRepository;
import com.resort.managementsystem.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ReservationService reservationService;

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + id));
    }

    public Staff saveStaff(Staff staff) throws IllegalArgumentException {
        Optional<Staff> existingStaff = staffRepository.findByEmail(staff.getEmail());
        if (existingStaff.isPresent() && (staff.getId() == null || !existingStaff.get().getId().equals(staff.getId()))) {
            throw new IllegalArgumentException("A staff member with this email already exists.");
        }
        return staffRepository.save(staff);
    }

    public Staff updateStaff(Long id, Staff staff) {
        Staff existingStaff = getStaffById(id);
        existingStaff.setFirstName(staff.getFirstName());
        existingStaff.setLastName(staff.getLastName());
        existingStaff.setEmail(staff.getEmail());
        existingStaff.setRole(staff.getRole());
        return saveStaff(existingStaff);
    }

    public void deleteStaff(Long id) {
        taskRepository.deleteByAssignedStaffId(id);
        staffRepository.deleteById(id);
    }

    public List<StaffSummary> getTopPerformingStaff() {
        List<Staff> staffList = staffRepository.findAll();
        return staffList.stream()
                .map(staff -> {
                    List<Reservation> reservations = reservationService.getReservationsByStaff(staff);
                    int bookingsHandled = reservations.size();
                    double revenueGenerated = reservations.stream()
                            .mapToDouble(Reservation::getTotalCost)
                            .sum();
                    return new StaffSummary(staff.getFirstName() + " " + staff.getLastName(), bookingsHandled, revenueGenerated);
                })
                .filter(summary -> summary.getBookingsHandled() > 0)
                .sorted(Comparator.comparingInt(StaffSummary::getBookingsHandled).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public static class StaffSummary {
        private String name;
        private int bookingsHandled;
        private double revenueGenerated;

        public StaffSummary(String name, int bookingsHandled, double revenueGenerated) {
            this.name = name;
            this.bookingsHandled = bookingsHandled;
            this.revenueGenerated = revenueGenerated;
        }

        public String getName() {
            return name;
        }

        public int getBookingsHandled() {
            return bookingsHandled;
        }

        public double getRevenueGenerated() {
            return revenueGenerated;
        }
    }
}
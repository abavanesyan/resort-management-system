package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.*;
import com.resort.managementsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    private long getTotalRooms() {
        return roomRepository.count();
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> searchReservations(String guestName, String roomNumber, LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = reservationRepository.findReservationsByCriteria(guestName, roomNumber, null, null);

        if (startDate != null || endDate != null) {
            reservations = reservations.stream()
                    .filter(reservation -> {
                        boolean matchesStart = startDate == null || !reservation.getCheckInDate().isBefore(startDate);
                        boolean matchesEnd = endDate == null || !reservation.getCheckOutDate().isAfter(endDate);
                        return matchesStart && matchesEnd;
                    })
                    .collect(Collectors.toList());
        }

        return reservations;
    }

    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation saveReservation(Reservation reservation) {
        if (reservation.getGuest() == null || reservation.getRoom() == null) {
            throw new IllegalArgumentException("Guest and Room must be selected.");
        }

        if (reservation.getCheckOutDate().isBefore(reservation.getCheckInDate()) ||
                reservation.getCheckOutDate().isEqual(reservation.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        Double discount = reservation.getDiscount();
        if (discount != null && (discount < 0.0 || discount > 1.0)) {
            throw new IllegalArgumentException("Discount must be between 0% and 100% (0.0 to 1.0)");
        }

        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                reservation.getRoom(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        overlappingReservations.removeIf(r -> r.getId() != null && r.getId().equals(reservation.getId()));

        if (!overlappingReservations.isEmpty()) {
            throw new IllegalArgumentException("Room is not available for the selected dates due to an existing reservation");
        }

        List<MaintenanceSchedule> overlappingMaintenanceSchedules = maintenanceScheduleRepository.findOverlappingSchedules(
                reservation.getRoom(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        if (!overlappingMaintenanceSchedules.isEmpty()) {
            throw new IllegalArgumentException("Room is scheduled for maintenance during the selected dates");
        }

        reservation.calculateTotalCost();

        Guest guest = reservation.getGuest();
        guest.setLastStayDate(reservation.getCheckOutDate());
        guest.setTotalStays(guest.getTotalStays() + 1);
        guestRepository.save(guest);

        Reservation savedReservation = reservationRepository.save(reservation);

        updateRoomStatus(reservation.getRoom());

        return savedReservation;
    }

    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + id));
        Guest guest = reservation.getGuest();
        Room room = reservation.getRoom();

        guest.setTotalStays(Math.max(0, guest.getTotalStays() - 1));
        List<Reservation> guestReservations = reservationRepository.findByGuestOrderByCheckOutDateDesc(guest);
        guestReservations.removeIf(r -> r.getId().equals(id));
        if (!guestReservations.isEmpty()) {
            guest.setLastStayDate(guestReservations.get(0).getCheckOutDate());
        } else {
            guest.setLastStayDate(null);
        }
        guestRepository.save(guest);

        reservationRepository.deleteById(id);

        updateRoomStatus(room);
    }

    private void updateRoomStatus(Room room) {
        LocalDate today = LocalDate.now();
        List<Reservation> activeReservations = reservationRepository.findOverlappingReservations(
                room, today, today
        );

        if (room.getStatus().equals("Under Maintenance")) {
            return;
        }

        if (!activeReservations.isEmpty()) {
            room.setStatus("Occupied");
        } else {
            room.setStatus("Available");
        }
        roomRepository.save(room);
    }

    // Dashboard Methods

    public int getTotalBookings() {
        return (int) reservationRepository.count();
    }

    public double getOccupancyRate() {
        LocalDate today = LocalDate.now();
        List<Reservation> activeReservations = reservationRepository.findAll().stream()
                .filter(r -> !r.getCheckInDate().isAfter(today) && !r.getCheckOutDate().isBefore(today))
                .collect(Collectors.toList());
        long occupiedRooms = activeReservations.stream()
                .map(Reservation::getRoom)
                .distinct()
                .count();
        long totalRooms = getTotalRooms();
        return totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0.0;
    }

    public List<Integer> getBookingsTrend() {
        LocalDate today = LocalDate.now();
        List<Integer> trend = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            long count = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isBefore(startOfMonth) && !r.getCheckInDate().isAfter(endOfMonth))
                    .count();
            trend.add((int) count);
        }
        return trend;
    }

    public List<Integer> getOccupancyTrend() {
        LocalDate today = LocalDate.now();
        List<Integer> trend = new ArrayList<>();
        for (int i = 3; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate midMonth = month.atDay(15);
            List<Reservation> activeReservations = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isAfter(midMonth) && !r.getCheckOutDate().isBefore(midMonth))
                    .collect(Collectors.toList());
            long occupiedRooms = activeReservations.stream()
                    .map(Reservation::getRoom)
                    .distinct()
                    .count();
            long totalRooms = getTotalRooms();
            trend.add((int) (totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0));
        }
        return trend;
    }

    public List<Integer> getBookingsOverTime() {
        LocalDate today = LocalDate.now();
        List<Integer> bookings = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            long count = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isBefore(startOfMonth) && !r.getCheckInDate().isAfter(endOfMonth))
                    .count();
            bookings.add((int) count);
        }
        return bookings;
    }

    public List<Double> getRevenueOverTime() {
        LocalDate today = LocalDate.now();
        List<Double> revenue = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            double monthlyRevenue = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isBefore(startOfMonth) && !r.getCheckInDate().isAfter(endOfMonth))
                    .mapToDouble(Reservation::getTotalCost)
                    .sum();
            revenue.add(monthlyRevenue);
        }
        return revenue;
    }

    public List<Integer> getOccupancyOverTime() {
        LocalDate today = LocalDate.now();
        List<Integer> occupancy = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate midMonth = month.atDay(15);
            List<Reservation> activeReservations = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isAfter(midMonth) && !r.getCheckOutDate().isBefore(midMonth))
                    .collect(Collectors.toList());
            long occupiedRooms = activeReservations.stream()
                    .map(Reservation::getRoom)
                    .distinct()
                    .count();
            long totalRooms = getTotalRooms();
            occupancy.add((int) (totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0));
        }
        return occupancy;
    }

    public List<Integer> getBookingSources() {
        Map<String, Long> sources = reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getBookingSource() != null ? r.getBookingSource() : "Unknown",
                        Collectors.counting()
                ));
        return Arrays.asList(
                sources.getOrDefault("Online", 0L).intValue(),
                sources.getOrDefault("Walk-In", 0L).intValue(),
                sources.getOrDefault("Agency", 0L).intValue()
        );
    }

    public Map<String, Double> getRevenueByRoomType() {
        return reservationRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        r -> r.getRoom().getRoomType(),
                        Collectors.summingDouble(Reservation::getTotalCost)
                ));
    }

    public double getTotalRevenue() {
        return reservationRepository.findAll().stream()
                .mapToDouble(Reservation::getTotalCost)
                .sum();
    }

    public List<Reservation> getReservationsByStaff(Staff staff) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStaff() != null && r.getStaff().getId().equals(staff.getId()))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getReservationTrends() {
        Map<String, Long> trends = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.from(today.minusMonths(i));
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth();
            long count = reservationRepository.findAll().stream()
                    .filter(r -> !r.getCheckInDate().isBefore(startOfMonth) && !r.getCheckOutDate().isAfter(endOfMonth))
                    .count();
            trends.put(month.toString(), count);
        }
        return trends;
    }
}
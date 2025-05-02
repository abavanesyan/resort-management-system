package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> searchRooms(String roomType, String status, Integer capacity, String smokingPolicy, String viewType, Integer floorNumber) {
        return roomRepository.findRoomsByCriteria(roomType, status, capacity, smokingPolicy, viewType, floorNumber);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public long countAvailableRooms() {
        return roomRepository.countByStatus("Available");
    }

    public double calculateOccupancyRate() {
        long totalRooms = roomRepository.count();
        if (totalRooms == 0) {
            return 0.0;
        }
        long occupiedRooms = roomRepository.countByStatus("Occupied");
        return (double) occupiedRooms / totalRooms * 100;
    }
}
package com.resort.managementsystem.service;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Optional<Guest> getGuestById(Long id) {
        return guestRepository.findById(id);
    }

    public Guest saveGuest(Guest guest) {
        return guestRepository.save(guest);
    }

    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }

    public List<Guest> findFrequentGuests(int limit) {
        return guestRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "totalStays"))
        ).getContent();
    }

    public List<Guest> searchGuests(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllGuests();
        }
        return guestRepository.searchGuests(query);
    }
}
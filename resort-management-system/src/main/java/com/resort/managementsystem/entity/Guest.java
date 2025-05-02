package com.resort.managementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Column(nullable = false)
    private String phoneNumber;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @Column(nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Column(nullable = false)
    private LocalDate checkOutDate;

    // Optional Fields (General Personal Info)
    @Column
    private String address;

    @PastOrPresent(message = "Date of birth must be in the past or present")
    @Column
    private LocalDate dateOfBirth;

    @Column
    private String nationality;

    @Column
    private String emergencyContact;

    // Optional Fields (Preferences)
    @Column
    private String roomTypePreference; // e.g., "Suite, Standard"

    @Column
    private String bedTypePreference; // e.g., "King, Twin"

    @Column
    private String smokingPreference; // e.g., "Non-smoking, Smoking"

    @Column
    private String dietaryPreferences; // e.g., "Vegetarian, Gluten-free"

    @Column
    private String communicationMethod; // e.g., "Email, Phone"

    @Column
    private String viewPreference; // e.g., "Ocean, Garden"

    // Optional Fields (Hobbies and Interests)
    @Column
    private String hobbies; // e.g., "Reading, Hiking, Swimming"

    @Column
    private String favoriteActivities; // e.g., "Spa, Golf, Tours"

    @Column
    private String favoriteDestination; // e.g., "Paris, Maldives"

    // Optional Fields (Stay Details)
    @Column
    private LocalDate lastStayDate;

    @Column
    private Integer totalStays = 0;

    @Column
    private String loyaltyProgramStatus; // e.g., "Bronze, Silver, Gold"

    @Column
    private String guestNotes;

    @Column
    private LocalDateTime lastUpdated;

    @Column
    private String updatedBy; // Staff who last updated the record

    // Photo Field
    @Lob
    @Column
    private byte[] photo;

    // Constructors
    public Guest() {}

    public Guest(String name, String email, String phoneNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getRoomTypePreference() { return roomTypePreference; }
    public void setRoomTypePreference(String roomTypePreference) { this.roomTypePreference = roomTypePreference; }

    public String getBedTypePreference() { return bedTypePreference; }
    public void setBedTypePreference(String bedTypePreference) { this.bedTypePreference = bedTypePreference; }

    public String getSmokingPreference() { return smokingPreference; }
    public void setSmokingPreference(String smokingPreference) { this.smokingPreference = smokingPreference; }

    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }

    public String getCommunicationMethod() { return communicationMethod; }
    public void setCommunicationMethod(String communicationMethod) { this.communicationMethod = communicationMethod; }

    public String getViewPreference() { return viewPreference; }
    public void setViewPreference(String viewPreference) { this.viewPreference = viewPreference; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getFavoriteActivities() { return favoriteActivities; }
    public void setFavoriteActivities(String favoriteActivities) { this.favoriteActivities = favoriteActivities; }

    public String getFavoriteDestination() { return favoriteDestination; }
    public void setFavoriteDestination(String favoriteDestination) { this.favoriteDestination = favoriteDestination; }

    public LocalDate getLastStayDate() { return lastStayDate; }
    public void setLastStayDate(LocalDate lastStayDate) { this.lastStayDate = lastStayDate; }

    public Integer getTotalStays() { return totalStays; }
    public void setTotalStays(Integer totalStays) { this.totalStays = totalStays; }

    public String getLoyaltyProgramStatus() { return loyaltyProgramStatus; }
    public void setLoyaltyProgramStatus(String loyaltyProgramStatus) { this.loyaltyProgramStatus = loyaltyProgramStatus; }

    public String getGuestNotes() { return guestNotes; }
    public void setGuestNotes(String guestNotes) { this.guestNotes = guestNotes; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }
}

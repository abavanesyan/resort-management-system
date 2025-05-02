package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.repository.GuestRepository;
import com.resort.managementsystem.service.GuestService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/web/guests")
public class GuestController {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private GuestService guestService;

    @GetMapping
    public String getAllGuests(@RequestParam(defaultValue = "list") String view,
                               @RequestParam(required = false) String searchQuery,
                               Model model) {
        model.addAttribute("guests", guestService.searchGuests(searchQuery));
        model.addAttribute("view", view);
        model.addAttribute("searchQuery", searchQuery);
        return "guests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("guest", new Guest());
        return "guests/create";
    }

    @PostMapping
    public String createGuest(@Valid @ModelAttribute("guest") Guest guest,
                              BindingResult result,
                              @RequestParam("photoFile") MultipartFile photoFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "guests/create";
        }
        try {
            if (!photoFile.isEmpty()) {
                guest.setPhoto(photoFile.getBytes());
            }
            guestService.saveGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest created successfully!");
            return "redirect:/web/guests";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An error occurred while uploading the photo: " + e.getMessage());
            return "guests/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the guest: " + e.getMessage());
            return "guests/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Guest guest = guestService.getGuestById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
        model.addAttribute("guest", guest);
        if (guest.getPhoto() != null) {
            model.addAttribute("photoBase64", Base64.getEncoder().encodeToString(guest.getPhoto()));
        }
        return "guests/edit";
    }

    @PostMapping("/update/{id}")
    public String updateGuest(@PathVariable Long id,
                              @Valid @ModelAttribute("guest") Guest guest,
                              BindingResult result,
                              @RequestParam("photoFile") MultipartFile photoFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "guests/edit";
        }
        guest.setId(id);
        try {
            if (!photoFile.isEmpty()) {
                guest.setPhoto(photoFile.getBytes());
            } else {
                Guest existingGuest = guestService.getGuestById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
                guest.setPhoto(existingGuest.getPhoto());
            }
            guestService.saveGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest updated successfully!");
            return "redirect:/web/guests";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "An error occurred while uploading the photo: " + e.getMessage());
            return "guests/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the guest: " + e.getMessage());
            return "guests/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteGuest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            guestService.deleteGuest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Guest deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the guest: " + e.getMessage());
        }
        return "redirect:/web/guests";
    }

    @GetMapping("/view/{id}")
    public String viewGuest(@PathVariable Long id, Model model) {
        Guest guest = guestService.getGuestById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + id));
        model.addAttribute("guest", guest);
        if (guest.getPhoto() != null) {
            model.addAttribute("photoBase64", Base64.getEncoder().encodeToString(guest.getPhoto()));
        }
        return "guests/view";
    }

    @GetMapping("/download")
    public ResponseEntity<String> downloadGuestsCsv() {
        List<Guest> guests = guestRepository.findAll();
        StringWriter writer = new StringWriter();
        PrintWriter csvWriter = new PrintWriter(writer);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        csvWriter.println("ID,Name,Email,Phone,Check-in,Check-out,Address,Date of Birth,Nationality,Emergency Contact," +
                "Room Type,Bed Type,Smoking,Dietary,Communication,View,Hobbies,Favorite Activities,Favorite Destination," +
                "Last Stay,Total Stays,Loyalty Status,Notes,Last Updated,Updated By");

        for (Guest g : guests) {
            csvWriter.printf("\"%d\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                            "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                            "\"%d\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    g.getId(),
                    g.getName(),
                    g.getEmail(),
                    g.getPhoneNumber(),
                    g.getCheckInDate() != null ? g.getCheckInDate().format(dateFmt) : "",
                    g.getCheckOutDate() != null ? g.getCheckOutDate().format(dateFmt) : "",
                    g.getAddress(),
                    g.getDateOfBirth() != null ? g.getDateOfBirth().format(dateFmt) : "",
                    g.getNationality(),
                    g.getEmergencyContact(),
                    g.getRoomTypePreference(),
                    g.getBedTypePreference(),
                    g.getSmokingPreference(),
                    g.getDietaryPreferences(),
                    g.getCommunicationMethod(),
                    g.getViewPreference(),
                    g.getHobbies(),
                    g.getFavoriteActivities(),
                    g.getFavoriteDestination(),
                    g.getLastStayDate() != null ? g.getLastStayDate().format(dateFmt) : "",
                    g.getTotalStays() != null ? g.getTotalStays() : 0,
                    g.getLoyaltyProgramStatus(),
                    g.getGuestNotes(),
                    g.getLastUpdated() != null ? g.getLastUpdated().format(dateTimeFmt) : "",
                    g.getUpdatedBy()
            );
        }

        csvWriter.flush();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guests.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(writer.toString());
    }

    @GetMapping("/download_pdf")
    public ResponseEntity<byte[]> downloadGuestsPdf() {
        List<Guest> guests = guestRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Guest List\n\n"));

            for (Guest g : guests) {
                document.add(new Paragraph(
                        "ID: " + g.getId() +
                                "\nName: " + g.getName() +
                                "\nEmail: " + g.getEmail() +
                                "\nPhone: " + g.getPhoneNumber() +
                                "\nCheck-in: " + (g.getCheckInDate() != null ? g.getCheckInDate().toString() : "") +
                                "\nCheck-out: " + (g.getCheckOutDate() != null ? g.getCheckOutDate().toString() : "") +
                                "\nAddress: " + g.getAddress() +
                                "\nNationality: " + g.getNationality() +
                                "\nLoyalty Status: " + g.getLoyaltyProgramStatus() +
                                "\n\n"
                ));
            }

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guests.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (DocumentException e) {
            return ResponseEntity.status(500).body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}

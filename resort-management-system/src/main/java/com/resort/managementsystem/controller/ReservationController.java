package com.resort.managementsystem.controller;

import com.itextpdf.text.DocumentException;
import com.resort.managementsystem.entity.Guest;
import com.resort.managementsystem.entity.Reservation;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.GuestService;
import com.resort.managementsystem.service.ReservationService;
import com.resort.managementsystem.service.RoomService;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Controller
@RequestMapping("/web/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private GuestService guestService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllReservations(
            @RequestParam(value = "guestName", required = false) String guestName,
            @RequestParam(value = "roomNumber", required = false) String roomNumber,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        LocalDate effectiveStartDate = (startDate != null) ? startDate : LocalDate.of(1900, 1, 1);
        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.of(9999, 12, 31);

        model.addAttribute("reservations", reservationService.searchReservations(guestName, roomNumber, startDate, endDate));
        model.addAttribute("guestName", guestName);
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "reservations/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reservation", new Reservation());
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservations/create";
    }

    @PostMapping
    public String createReservation(@Valid @ModelAttribute("reservation") Reservation reservation, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Long guestId = reservation.getGuest() != null ? reservation.getGuest().getId() : null;
        Long roomId = reservation.getRoom() != null ? reservation.getRoom().getId() : null;

        if (guestId != null) {
            Guest guest = guestService.getGuestById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));
            reservation.setGuest(guest);
        } else {
            model.addAttribute("errorMessage", "Please select a guest.");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            reservation.setRoom(room);
        } else {
            model.addAttribute("errorMessage", "Please select a room.");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        // Validate discount
        Double discount = reservation.getDiscount();
        if (discount != null && (discount < 0.0 || discount > 1.0)) {
            model.addAttribute("errorMessage", "Discount must be between 0% and 100% (0.0 to 1.0)");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        if (result.hasErrors()) {
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }

        try {
            reservationService.saveReservation(reservation);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation created successfully!");
            return "redirect:/web/reservations";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "Failed to save reservation: Ensure all required fields are valid. " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the reservation: " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation ID: " + id));
        model.addAttribute("reservation", reservation);
        model.addAttribute("guests", guestService.getAllGuests());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservations/edit";
    }

    @PostMapping("/update/{id}")
    public String updateReservation(@PathVariable Long id, @Valid @ModelAttribute("reservation") Reservation reservation, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Long guestId = reservation.getGuest() != null ? reservation.getGuest().getId() : null;
        Long roomId = reservation.getRoom() != null ? reservation.getRoom().getId() : null;

        if (guestId != null) {
            Guest guest = guestService.getGuestById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest ID: " + guestId));
            reservation.setGuest(guest);
        }

        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            reservation.setRoom(room);
        }

        // Validate discount
        Double discount = reservation.getDiscount();
        if (discount != null && (discount < 0.0 || discount > 1.0)) {
            model.addAttribute("errorMessage", "Discount must be between 0% and 100% (0.0 to 1.0)");
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/edit";
        }

        if (result.hasErrors()) {
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/edit";
        }

        reservation.setId(id);
        try {
            reservationService.saveReservation(reservation);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation updated successfully!");
            return "redirect:/web/reservations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the reservation: " + e.getMessage());
            model.addAttribute("guests", guestService.getAllGuests());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservations/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reservation deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the reservation: " + e.getMessage());
        }
        return "redirect:/web/reservations";
    }

    @GetMapping("/download")
    public void downloadReservationsCsv(HttpServletResponse response) throws IOException {
        List<Reservation> reservations = reservationService.getAllReservations();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=reservations.csv");

        PrintWriter writer = response.getWriter();

        // CSV Header
        writer.println("ID,Check-In Date,Check-Out Date,Total Cost,Discount,Guest Name,Room Number");

        // CSV Rows
        for (Reservation reservation : reservations) {
            String line = String.format("%d,%s,%s,%.2f,%.2f,%s,%s",
                    reservation.getId(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    reservation.getTotalCost(),
                    reservation.getDiscount(),
                    reservation.getGuest() != null ? reservation.getGuest().getName() : "N/A",
                    reservation.getRoom() != null ? reservation.getRoom().getNumber() : "N/A"
            );
            writer.println(line);
        }

        writer.flush();
    }

    @GetMapping("/download/pdf")
    public void downloadReservationsPdf(HttpServletResponse response) throws IOException, DocumentException {
        List<Reservation> reservations = reservationService.getAllReservations();

        // Set the content type and header for PDF download
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reservations.pdf");

        // Create a PDF document using iText
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Add a title
        com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Reservations List", font);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(title);
        document.add(new com.itextpdf.text.Chunk("\n"));

        // Add a table with reservation data
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.addCell("ID");
        table.addCell("Check-In Date");
        table.addCell("Check-Out Date");
        table.addCell("Total Cost");
        table.addCell("Discount");
        table.addCell("Guest Name");
        table.addCell("Room Number");

        // Populate the table with reservation data
        for (Reservation reservation : reservations) {
            table.addCell(String.valueOf(reservation.getId()));
            table.addCell(reservation.getCheckInDate().toString());
            table.addCell(reservation.getCheckOutDate().toString());
            table.addCell(String.format("%.2f", reservation.getTotalCost()));
            table.addCell(String.format("%.2f", reservation.getDiscount()));
            table.addCell(reservation.getGuest() != null ? reservation.getGuest().getName() : "N/A");
            table.addCell(reservation.getRoom() != null ? reservation.getRoom().getNumber() : "N/A");
        }

        // Add the table to the document
        document.add(table);

        // Close the document
        document.close();
    }
}

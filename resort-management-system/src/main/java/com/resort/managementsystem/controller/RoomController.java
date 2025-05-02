package com.resort.managementsystem.controller;

import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/web/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllRooms(
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "smokingPolicy", required = false) String smokingPolicy,
            @RequestParam(value = "viewType", required = false) String viewType,
            @RequestParam(value = "floorNumber", required = false) Integer floorNumber,
            Model model) {
        List<Room> rooms;
        if (roomType != null || status != null || capacity != null || smokingPolicy != null || viewType != null || floorNumber != null) {
            rooms = roomService.searchRooms(
                    roomType != null && !roomType.isEmpty() ? roomType : null,
                    status != null && !status.isEmpty() ? status : null,
                    capacity,
                    smokingPolicy != null && !smokingPolicy.isEmpty() ? smokingPolicy : null,
                    viewType != null && !viewType.isEmpty() ? viewType : null,
                    floorNumber
            );
        } else {
            rooms = roomService.getAllRooms();
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomType", roomType);
        model.addAttribute("status", status);
        model.addAttribute("capacity", capacity);
        model.addAttribute("smokingPolicy", smokingPolicy);
        model.addAttribute("viewType", viewType);
        model.addAttribute("floorNumber", floorNumber);
        return "rooms/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room()); // Do not set id here
        return "rooms/create";
    }

    @PostMapping
    public String createRoom(@Valid @ModelAttribute("room") Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rooms/create";
        }
        try {
            roomService.saveRoom(room); // Hibernate will generate the id
            redirectAttributes.addFlashAttribute("successMessage", "Room created successfully!");
            return "redirect:/web/rooms";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the room: " + e.getMessage());
            return "rooms/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + id));
        model.addAttribute("room", room);
        return "rooms/edit";
    }

    @PostMapping("/update/{id}")
    public String updateRoom(@PathVariable Long id, @Valid @ModelAttribute("room") Room room, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "rooms/edit";
        }
        room.setId(id);
        try {
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Room updated successfully!");
            return "redirect:/web/rooms";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the room: " + e.getMessage());
            return "rooms/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the room: " + e.getMessage());
        }
        return "redirect:/web/rooms";
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadRoomsAsCsv() {
        List<Room> rooms = roomService.getAllRooms();

        // Create the CSV content
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Room Number,Rate,Room Type,Capacity,Floor Number,View Type,Amenities,Status,Last Maintenance Date,Smoking Policy,Size,Accessibility Features\n");

        for (Room room : rooms) {
            csvContent.append(room.getId()).append(",")
                    .append(room.getNumber()).append(",")
                    .append(room.getRate()).append(",")
                    .append(room.getRoomType()).append(",")
                    .append(room.getCapacity()).append(",")
                    .append(room.getFloorNumber()).append(",")
                    .append(room.getViewType()).append(",")
                    .append(room.getAmenities()).append(",")
                    .append(room.getStatus()).append(",")
                    .append(room.getLastMaintenanceDate() != null ? room.getLastMaintenanceDate() : "").append(",")
                    .append(room.getSmokingPolicy()).append(",")
                    .append(room.getSize()).append(",")
                    .append(room.getAccessibilityFeatures()).append("\n");
        }

        // Convert the StringBuilder to byte array
        byte[] csvBytes = csvContent.toString().getBytes(StandardCharsets.UTF_8);

        // Create a ByteArrayResource from the byte array
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        // Return as downloadable file with proper headers
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(resource);
    }

    @GetMapping("/download_pdf")
    public ResponseEntity<byte[]> downloadRoomsAsPdf() {
        List<Room> rooms = roomService.getAllRooms();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create the PDF document
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add Title to PDF
            document.add(new Paragraph("Rooms List"));

            // Table Setup
            PdfPTable table = new PdfPTable(7); // Adjust column count as needed
            table.addCell("ID");
            table.addCell("Room Number");
            table.addCell("Rate");
            table.addCell("Room Type");
            table.addCell("Capacity");
            table.addCell("Floor Number");
            table.addCell("View Type");

            // Loop over rooms and add rows to the table
            for (Room room : rooms) {
                table.addCell(String.valueOf(room.getId()));
                table.addCell(room.getNumber());
                table.addCell(String.valueOf(room.getRate()));
                table.addCell(room.getRoomType());
                table.addCell(String.valueOf(room.getCapacity()));
                table.addCell(String.valueOf(room.getFloorNumber()));
                table.addCell(room.getViewType());
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        // Return the PDF file
        byte[] pdfBytes = outputStream.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}

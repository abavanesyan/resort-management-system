package com.resort.managementsystem.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.resort.managementsystem.entity.MaintenanceSchedule;
import com.resort.managementsystem.entity.Room;
import com.resort.managementsystem.service.MaintenanceScheduleService;
import com.resort.managementsystem.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/web/maintenance-schedules")
public class MaintenanceScheduleController {

    @Autowired
    private MaintenanceScheduleService maintenanceScheduleService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String getAllSchedules(Model model) {
        model.addAttribute("schedules", maintenanceScheduleService.getAllSchedules());
        return "maintenance-schedules/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("schedule", new MaintenanceSchedule());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "maintenance-schedules/create";
    }

    @PostMapping
    public String createSchedule(@Valid @ModelAttribute("schedule") MaintenanceSchedule schedule, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Long roomId = schedule.getRoom() != null ? schedule.getRoom().getId() : null;
        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            schedule.setRoom(room);
        }

        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/create";
        }

        try {
            maintenanceScheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule created successfully!");
            return "redirect:/web/maintenance-schedules";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while saving the maintenance schedule: " + e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        MaintenanceSchedule schedule = maintenanceScheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid maintenance schedule ID: " + id));
        model.addAttribute("schedule", schedule);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "maintenance-schedules/edit";
    }

    @PostMapping("/update/{id}")
    public String updateSchedule(@PathVariable Long id, @Valid @ModelAttribute("schedule") MaintenanceSchedule schedule, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        Long roomId = schedule.getRoom() != null ? schedule.getRoom().getId() : null;
        if (roomId != null) {
            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room ID: " + roomId));
            schedule.setRoom(room);
        }

        if (result.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/edit";
        }

        schedule.setId(id);
        try {
            maintenanceScheduleService.saveSchedule(schedule);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule updated successfully!");
            return "redirect:/web/maintenance-schedules";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating the maintenance schedule: " + e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "maintenance-schedules/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            maintenanceScheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute("successMessage", "Maintenance schedule deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the maintenance schedule: " + e.getMessage());
        }
        return "redirect:/web/maintenance-schedules";
    }

    // CSV download
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadSchedulesAsCsv() {
        List<MaintenanceSchedule> schedules = maintenanceScheduleService.getAllSchedules();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8)) {
            writer.write("ID,Room,Start Date,End Date,Description\n");
            for (MaintenanceSchedule schedule : schedules) {
                writer.write(schedule.getId() + ",");
                writer.write(schedule.getRoom() != null ? schedule.getRoom().getNumber() : "N/A");
                writer.write(",");
                writer.write(schedule.getStartDate().toString());
                writer.write(",");
                writer.write(schedule.getEndDate().toString());
                writer.write(",");
                writer.write(schedule.getDescription() != null ? schedule.getDescription() : "N/A");
                writer.write("\n");
            }
            writer.flush();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        byte[] content = byteArrayOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDisposition(ContentDisposition.attachment().filename("maintenance_schedules.csv").build());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    // PDF download
    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadSchedulesAsPdf() {
        List<MaintenanceSchedule> schedules = maintenanceScheduleService.getAllSchedules();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Maintenance Schedules", font);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 2, 2, 3});

            Stream.of("ID", "Room", "Start Date", "End Date", "Description").forEach(headerTitle -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setBorderWidth(2);
                header.setPhrase(new Phrase(headerTitle));
                table.addCell(header);
            });

            for (MaintenanceSchedule schedule : schedules) {
                table.addCell(schedule.getId().toString());
                table.addCell(schedule.getRoom() != null
                        ? schedule.getRoom().getNumber() + " (" + schedule.getRoom().getRoomType() + ")"
                        : "N/A");
                table.addCell(schedule.getStartDate().toString());
                table.addCell(schedule.getEndDate().toString());
                table.addCell(schedule.getDescription() != null ? schedule.getDescription() : "N/A");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        byte[] pdfContent = out.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("maintenance_schedules.pdf").build());

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}

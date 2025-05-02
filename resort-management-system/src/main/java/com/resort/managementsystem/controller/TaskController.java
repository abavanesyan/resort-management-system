package com.resort.managementsystem.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.resort.managementsystem.entity.Task;
import com.resort.managementsystem.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping("/{staffId}")
    public ResponseEntity<Task> createTask(@RequestBody Task task, @PathVariable Long staffId) {
        Task createdTask = taskService.createTask(task, staffId);
        return ResponseEntity.ok(createdTask);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<Task>> getTasksByStaffId(@PathVariable Long staffId) {
        List<Task> tasks = taskService.getTasksByStaffId(staffId);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download")
    public ResponseEntity<String> downloadTasksAsCSV() throws IOException {
        List<Task> tasks = taskService.getAllTasks();

        if (tasks.isEmpty()) {
            return ResponseEntity.badRequest().body("No tasks available for download.");
        }

        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,Description,Status,Due Date,Assigned Staff\n");

        for (Task task : tasks) {
            csvContent.append(task.getId()).append(",");
            csvContent.append(task.getDescription()).append(",");
            csvContent.append(task.getStatus()).append(",");
            csvContent.append(task.getDueDate()).append(",");
            csvContent.append(task.getAssignedStaff().getFirstName()).append(" ")
                    .append(task.getAssignedStaff().getLastName()).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tasks.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent.toString());
    }

    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadTasksAsPDF() {
        List<Task> tasks = taskService.getAllTasks();

        if (tasks.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph title = new Paragraph("Task List", font);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 4, 2, 2, 3});

            addTableHeader(table, "ID", "Description", "Status", "Due Date", "Assigned Staff");

            for (Task task : tasks) {
                table.addCell(String.valueOf(task.getId()));
                table.addCell(task.getDescription());
                table.addCell(task.getStatus());
                table.addCell(task.getDueDate().toString());
                table.addCell(task.getAssignedStaff().getFirstName() + " " + task.getAssignedStaff().getLastName());
            }

            document.add(table);
            document.close();

            byte[] pdfBytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "tasks.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Phrase(header));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }
    }
}

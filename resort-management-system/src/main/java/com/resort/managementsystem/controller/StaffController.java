package com.resort.managementsystem.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.resort.managementsystem.entity.Staff;
import com.resort.managementsystem.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    @Autowired
    private StaffService staffService;

    @GetMapping
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Staff> getStaffById(@PathVariable Long id) {
        Staff staff = staffService.getStaffById(id);
        return ResponseEntity.ok(staff);
    }

    @PostMapping
    public ResponseEntity<Staff> createStaff(@RequestBody Staff staff) {
        Staff createdStaff = staffService.saveStaff(staff);
        return ResponseEntity.ok(createdStaff);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Staff> updateStaff(@PathVariable Long id, @RequestBody Staff staff) {
        Staff updatedStaff = staffService.updateStaff(id, staff);
        return ResponseEntity.ok(updatedStaff);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    // CSV download
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadStaffAsCSV() throws IOException {
        List<Staff> staffList = staffService.getAllStaff();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(byteArrayOutputStream);

        writer.write("ID,First Name,Last Name,Email,Role\n");
        for (Staff staff : staffList) {
            writer.write(String.format("%d,%s,%s,%s,%s\n", staff.getId(), staff.getFirstName(), staff.getLastName(), staff.getEmail(), staff.getRole()));
        }

        writer.flush();
        byte[] csvData = byteArrayOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=staff_data.csv");
        headers.add("Content-Type", "text/csv");

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }

    // PDF download
    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadStaffAsPDF() throws IOException, DocumentException {
        List<Staff> staffList = staffService.getAllStaff();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph title = new Paragraph("Staff List", font);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 2, 4, 2});

        Stream.of("ID", "First Name", "Last Name", "Email", "Role")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });

        for (Staff staff : staffList) {
            table.addCell(String.valueOf(staff.getId()));
            table.addCell(staff.getFirstName());
            table.addCell(staff.getLastName());
            table.addCell(staff.getEmail());
            table.addCell(staff.getRole());
        }

        document.add(table);
        document.close();

        byte[] pdfData = byteArrayOutputStream.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=staff_data.pdf");
        headers.add("Content-Type", "application/pdf");

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }
}

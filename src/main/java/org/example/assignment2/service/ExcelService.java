package org.example.assignment2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.assignment2.model.Course;
import org.example.assignment2.model.Enrollment;
import org.example.assignment2.model.User;
import org.example.assignment2.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelService {

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public void exportEnrollmentsToExcel(List<Enrollment> enrollments, HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Enrollments_Export_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enrollments");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Course Title", "Learner Name", "Learner Email", "Status", "Fee (VND)", "Enrolled At"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowCount = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Enrollment e : enrollments) {
                Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(e.getId() != null ? e.getId() : 0);
                row.createCell(1).setCellValue(e.getCourse() != null ? e.getCourse().getTitle() : "");
                String fullName = e.getFullName() != null ? e.getFullName() : (e.getUser() != null ? e.getUser().getFullName() : "");
                row.createCell(2).setCellValue(fullName);
                String email = e.getEmail() != null ? e.getEmail() : (e.getUser() != null ? e.getUser().getEmail() : "");
                row.createCell(3).setCellValue(email);
                row.createCell(4).setCellValue(e.getStatus() != null ? e.getStatus() : "Pending");
                row.createCell(5).setCellValue(e.getFee() != null ? e.getFee().doubleValue() : 0.0);
                String dateStr = e.getEnrolledAt() != null ? e.getEnrolledAt().format(dateFormatter) : "";
                row.createCell(6).setCellValue(dateStr);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        }
    }

    /**
     * Import enrollments from Excel file for a specific course.
     * Expected columns: Email (required), Full Name (optional), Status (optional, defaults to Pending)
     * Returns map with "imported" (int count) and "errors" (List<String>)
     */
    public Map<String, Object> importEnrollmentsFromExcel(InputStream inputStream, Course course) throws IOException {
        List<String> errors = new ArrayList<>();
        int importedCount = 0;

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                errors.add("Excel file has no sheets");
                return Map.of("imported", 0, "errors", errors);
            }

            // Find column indices from header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                errors.add("Excel file has no header row");
                return Map.of("imported", 0, "errors", errors);
            }

            int emailCol = -1, fullNameCol = -1, statusCol = -1;
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell == null) continue;
                String header = cell.getStringCellValue().trim().toLowerCase();
                if (header.contains("email")) emailCol = i;
                else if (header.contains("name") || header.contains("full")) fullNameCol = i;
                else if (header.contains("status")) statusCol = i;
            }

            if (emailCol == -1) {
                errors.add("Cannot find 'Email' column in header row");
                return Map.of("imported", 0, "errors", errors);
            }

            // Process data rows
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                String email = getCellStringValue(row.getCell(emailCol));
                if (email == null || email.isBlank()) {
                    errors.add("Row " + (rowIdx + 1) + ": Email is empty, skipped");
                    continue;
                }

                User user = userService.findByEmail(email.trim());
                if (user == null) {
                    errors.add("Row " + (rowIdx + 1) + ": User not found with email '" + email + "', skipped");
                    continue;
                }

                String fullName = fullNameCol >= 0 ? getCellStringValue(row.getCell(fullNameCol)) : null;
                if (fullName == null || fullName.isBlank()) {
                    fullName = user.getFullName();
                }

                String status = statusCol >= 0 ? getCellStringValue(row.getCell(statusCol)) : null;
                if (status == null || status.isBlank()) {
                    status = "Pending";
                }

                Enrollment enrollment = new Enrollment();
                enrollment.setCourse(course);
                enrollment.setUser(user);
                enrollment.setFullName(fullName);
                enrollment.setEmail(user.getEmail());
                enrollment.setFee(course.getPrice());
                enrollment.setStatus(status);
                enrollment.setProgress(0.0);
                enrollment.setEnrolledAt(LocalDateTime.now());

                enrollmentRepository.save(enrollment);
                importedCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("imported", importedCount);
        result.put("errors", errors);
        return result;
    }

    /**
     * Generate a blank import template Excel file for download.
     * Contains header row and 2 sample data rows.
     */
    public void generateImportTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=Enrollment_Import_Template.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Enrollments");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Email", "Full Name", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample style (italic, gray text)
            CellStyle sampleStyle = workbook.createCellStyle();
            Font sampleFont = workbook.createFont();
            sampleFont.setItalic(true);
            sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            sampleStyle.setFont(sampleFont);

            // Sample row 1
            Row sample1 = sheet.createRow(1);
            String[][] sampleData = {
                {"student1@example.com", "Nguyen Van A", "Pending"},
                {"student2@example.com", "Tran Thi B", "Approved"}
            };
            for (int i = 0; i < sampleData[0].length; i++) {
                Cell cell = sample1.createCell(i);
                cell.setCellValue(sampleData[0][i]);
                cell.setCellStyle(sampleStyle);
            }

            // Sample row 2
            Row sample2 = sheet.createRow(2);
            for (int i = 0; i < sampleData[1].length; i++) {
                Cell cell = sample2.createCell(i);
                cell.setCellValue(sampleData[1][i]);
                cell.setCellStyle(sampleStyle);
            }

            // Instructions row
            Row noteRow = sheet.createRow(4);
            CellStyle noteStyle = workbook.createCellStyle();
            Font noteFont = workbook.createFont();
            noteFont.setColor(IndexedColors.RED.getIndex());
            noteFont.setFontHeightInPoints((short) 10);
            noteStyle.setFont(noteFont);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("* Xóa dữ liệu mẫu trước khi nhập. Email là bắt buộc. Status: Pending / Approved / Rejected");
            noteCell.setCellStyle(noteStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return null;
        }
    }
}
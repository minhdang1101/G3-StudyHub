package org.example.assignment2.controller;

import org.example.assignment2.model.Course;
import org.example.assignment2.model.Enrollment;
import org.example.assignment2.model.User;
import org.example.assignment2.service.CourseService;
import org.example.assignment2.service.EnrollmentService;
import org.example.assignment2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.assignment2.model.Setting;
import org.example.assignment2.service.ExcelService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/enrollments")
public class AdminEnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExcelService excelService;

    // 1. DANH SÁCH CÓ PHÂN QUYỀN VÀ PHÂN TRANG
    @GetMapping
    public String showEnrollmentList(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, 
            HttpServletRequest request,
            HttpSession session) { 
        User currentUser = getCurrentUser(request, session);
        
        if (currentUser == null) {
            return "redirect:/login"; 
        }

        Long filterCourseId = (courseId != null && courseId > 0) ? courseId : null;
        Long filterUserId = (userId != null && userId > 0) ? userId : null;
        String filterStatus = (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) ? status.trim() : null;
        String filterSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollmentPage = enrollmentService.searchEnrollmentsWithRole(
                filterCourseId, filterUserId, filterStatus, filterSearch, currentUser, pageable);
        
        List<Course> courses = courseService.getAllCourses();
        List<User> users = userService.getAllUsers(); 

        model.addAttribute("enrollmentPage", enrollmentPage);
        model.addAttribute("enrollments", enrollmentPage.getContent());
        model.addAttribute("courses", courses);
        model.addAttribute("users", users);
        
        // Metadata for pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", enrollmentPage.getTotalPages());
        model.addAttribute("totalElements", enrollmentPage.getTotalElements());
        model.addAttribute("pageSize", size);
        
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("selectedUserId", userId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);

        return "enrollment/enrollment-list";
    }

    @GetMapping("/add")
    public String showAddEnrollmentForm(Model model, HttpServletRequest request, HttpSession session) {
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        model.addAttribute("enrollment", new Enrollment()); 
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("isNew", true); 

        return "enrollment/enrollment-details";
    }

    @GetMapping("/{id}") 
    public String showEnrollmentDetails(@PathVariable("id") Long id, Model model, HttpServletRequest request, HttpSession session) {
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        Enrollment enrollment = enrollmentService.getEnrollmentById(id);
        if (enrollment == null) return "redirect:/admin/enrollments"; 

        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            if (enrollment.getCourse().getManager() == null || !enrollment.getCourse().getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn không có quyền thêm học viên vào khóa học này vì khóa học chưa có quản lý hoặc không thuộc quyền của bạn!");
            }
        }
        
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("isNew", false); 

        return "enrollment/enrollment-details";
    }
    
    @PostMapping("/save")
    public String saveEnrollment(
            @RequestParam(value = "enrollmentId", required = false) Long id,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam("status") String status,
            @RequestParam(value = "rejectNotes", required = false) String rejectNotes,
            HttpServletRequest request,
            HttpSession session) {
        
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        try {
            if (id == null) {
                enrollmentService.addNewEnrollmentByAdmin(courseId, userId, status, rejectNotes, currentUser);
            } else {
                enrollmentService.updateEnrollmentStatusAndNotes(id, status, rejectNotes, currentUser);
            }
            return "redirect:/admin/enrollments?success=saved";
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/enrollments?error=" + encodedMessage;
        }
    }

    // EXPORT EXCEL (LẤY TẤT CẢ KHÔNG PHÂN TRANG)
    @GetMapping("/export")
    public void exportToExcel(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            HttpServletRequest request,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) {
            response.sendRedirect("/login");
            return;
        }

        Long filterCourseId = (courseId != null && courseId > 0) ? courseId : null;
        Long filterUserId = (userId != null && userId > 0) ? userId : null;
        String filterStatus = (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) ? status.trim() : null;
        String filterSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<Enrollment> enrollments = enrollmentService.searchAllEnrollmentsWithRole(
                filterCourseId, filterUserId, filterStatus, filterSearch, currentUser);

        excelService.exportEnrollmentsToExcel(enrollments, response);
    }

    // DOWNLOAD IMPORT TEMPLATE
    @GetMapping("/template")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        excelService.generateImportTemplate(response);
    }

    // IMPORT EXCEL
    @PostMapping("/import")
    public String importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {

        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select an Excel file to import.");
            return "redirect:/admin/enrollments";
        }

        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "Course not found!");
            return "redirect:/admin/enrollments";
        }

        // Manager can only import to their assigned courses
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            if (course.getManager() == null || !course.getManager().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You don't have permission to import for this course or the course has no assigned manager!");
                return "redirect:/admin/enrollments";
            }
        }

        try {
            Map<String, Object> result = excelService.importEnrollmentsFromExcel(file.getInputStream(), course);
            int imported = (int) result.get("imported");
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) result.get("errors");

            String message = "Successfully imported " + imported + " enrollment(s) for course: " + course.getTitle();
            if (!errors.isEmpty()) {
                message += ". Warnings: " + String.join("; ", errors);
            }
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Import failed: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    // DELETE ENROLLMENT
    @PostMapping("/delete/{id}")
    public String deleteEnrollment(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpSession session) {

        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        try {
            enrollmentService.deleteEnrollment(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Enrollment deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }

        return "redirect:/admin/enrollments";
    }

    // CẬP NHẬT TRẠNG THÁI VÀ GHI CHÚ
    @PostMapping("/update")
    public String updateEnrollment(
            @RequestParam("enrollmentId") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "rejectNotes", required = false) String rejectNotes,
            HttpServletRequest request,
            HttpSession session) {
        
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        try {
            enrollmentService.updateEnrollmentStatusAndNotes(id, status, rejectNotes, currentUser);
            return "redirect:/admin/enrollments?success=updated";
        } catch (Exception e) {
            String encodedMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/admin/enrollments?error=" + encodedMessage;
        }
    }

    private User getCurrentUser(HttpServletRequest request, HttpSession session) {
        User mockManager = new User();
        mockManager.setId(2); 
        mockManager.setFullName("Test Manager");
    
        Setting role = new Setting();
        role.setId(2);
        role.setValue("MANAGER"); 
        role.setName("MANAGER");
        mockManager.setRole(role);
                                                                                                            
        return mockManager;
    }

}
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
import org.example.assignment2.service.ExcelService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

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

        boolean isManager = isManager(currentUser);

        Long filterCourseId = (courseId != null && courseId > 0) ? courseId : null;
        Long filterUserId = (userId != null && userId > 0) ? userId : null;
        String filterStatus = (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) ? status.trim() : null;
        String filterSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollmentPage = enrollmentService.searchEnrollmentsWithRole(
                filterCourseId, filterUserId, filterStatus, filterSearch, currentUser, pageable);
        
        List<Course> courses = isManager
                ? courseService.getCoursesByManagerId(currentUser.getId())
                : courseService.getAllCourses();
        List<User> users = isManager
                ? userService.getUsersByManagerCourses(currentUser.getId())
                : userService.getAllUsers();

        model.addAttribute("enrollmentPage", enrollmentPage);
        model.addAttribute("enrollments", enrollmentPage.getContent());
        model.addAttribute("courses", courses);
        model.addAttribute("users", users);
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

        boolean isManager = isManager(currentUser);

        model.addAttribute("enrollment", new Enrollment()); 
        model.addAttribute("courses", isManager
                ? courseService.getCoursesByManagerId(currentUser.getId())
                : courseService.getAllCourses());
        model.addAttribute("isNew", true); 

        return "enrollment/enrollment-details";
    }

    @GetMapping("/lookup-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lookupUserByEmail(
            @RequestParam String email) {
        User user = userService.findByEmail(email.trim());
        Map<String, Object> resp = new HashMap<>();
        if (user == null) {
            resp.put("found", false);
            resp.put("message", "Không tìm thấy người dùng với email: " + email);
            return ResponseEntity.status(404).body(resp);
        }
        resp.put("found", true);
        resp.put("userId", user.getId());
        resp.put("fullName", user.getFullName());
        return ResponseEntity.ok(resp);
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
        model.addAttribute("courses", isManager(currentUser)
                ? courseService.getCoursesByManagerId(currentUser.getId())
                : courseService.getAllCourses());
        model.addAttribute("users", isManager(currentUser)
                ? userService.getUsersByManagerCourses(currentUser.getId())
                : userService.getAllUsers());
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
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        try {
            if (id == null) {
                enrollmentService.addNewEnrollmentByAdmin(courseId, userId, status, rejectNotes, currentUser);
            } else {
                enrollmentService.updateEnrollmentStatusAndNotes(id, status, rejectNotes, currentUser);
            }
            redirectAttributes.addFlashAttribute("success", "Lưu thông tin đăng ký thành công!");
            return "redirect:/admin/enrollments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/enrollments";
        }
    }

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


    @GetMapping("/template")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        excelService.generateImportTemplate(response);
    }

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

    @PostMapping("/update")
    public String updateEnrollment(
            @RequestParam("enrollmentId") Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "rejectNotes", required = false) String rejectNotes,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser(request, session);
        if (currentUser == null) return "redirect:/login";

        try {
            enrollmentService.updateEnrollmentStatusAndNotes(id, status, rejectNotes, currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            return "redirect:/admin/enrollments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/enrollments";
        }
    }

    private boolean isManager(User user) {
        if (user == null || user.getRole() == null) return false;
        String v = user.getRole().getValue();
        return "MANAGER".equalsIgnoreCase(v) || "ROLE_MANAGER".equalsIgnoreCase(v);
    }

    private User getCurrentUser(HttpServletRequest request, HttpSession session) {
        return (User) session.getAttribute("user");
    }

}
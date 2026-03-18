package org.example.assignment2.service;

import org.example.assignment2.model.Enrollment;
import org.example.assignment2.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import org.example.assignment2.model.Course;
import org.example.assignment2.model.User;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> searchEnrollments(Long courseId, Long userId, String status, String keyword) {
        return enrollmentRepository.searchAdvanced(courseId, userId, status, keyword);
    }

    public List<Enrollment> getEnrollmentsByUserId(Long userId) {
        return enrollmentRepository.findByUser_Id(userId);
    }

    public Page<Enrollment> getEnrollmentsByUserIdPaged(Long userId, Pageable pageable) {
        return enrollmentRepository.findByUser_IdOrderByEnrolledAtDesc(userId, pageable);
    }

    public boolean isAlreadyEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsActiveEnrollment(userId, courseId);
    }

    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id).orElse(null);
    }

    public void updateEnrollment(Enrollment enrollment) {
        enrollmentRepository.save(enrollment);
    }

    public void markEnrollmentAsPaid(Long id) {
        Enrollment enrollment = getEnrollmentById(id);
        if (enrollment != null && "Pending".equalsIgnoreCase(enrollment.getStatus())) {
            enrollment.setStatus("Paid");
            enrollment.setLastUpdated(LocalDateTime.now());
            enrollmentRepository.save(enrollment);
        }
    }

    public Enrollment createInitialEnrollment(Long courseId, User currentUser) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) return null;

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setFee(course.getPrice());

        if (currentUser != null) {
            enrollment.setUser(currentUser);
            enrollment.setFullName(currentUser.getFullName());
            enrollment.setEmail(currentUser.getEmail());
            enrollment.setMobile(currentUser.getMobile());
        } else {
            User mockUser = new User();
            mockUser.setId(2);
            enrollment.setUser(mockUser);
        }
        return enrollment;
    }

    public void processEnrollmentSubmission(Enrollment enrollment, Long courseId) {
        Course course = courseService.getCourseById(courseId);
        enrollment.setCourse(course);
        enrollment.setFee(course.getPrice());
        enrollment.setStatus("Pending");
        enrollment.setProgress(0.0);
        enrollment.setEnrolledAt(LocalDateTime.now());

        if (enrollment.getUser() == null || enrollment.getUser().getId() == null) {
            User mockUser = new User();
            mockUser.setId(2);
            enrollment.setUser(mockUser);
        }

        enrollmentRepository.save(enrollment);
    }


    public void updateEnrollmentStatus(Long id, String status, String rejectNotes) {
        Enrollment existing = getEnrollmentById(id);
        if (existing != null) {
            existing.setStatus(status);

            if ("Rejected".equalsIgnoreCase(status)) {
                existing.setRejectNotes(rejectNotes);
            } else {
                existing.setRejectNotes(null);
            }

            existing.setLastUpdated(LocalDateTime.now());

            if (existing.getProgress() != null && existing.getProgress() >= 100.0 && existing.getCompletedAt() == null) {
                 existing.setCompletedAt(LocalDateTime.now());
            }

            enrollmentRepository.save(existing);
        }
    }

    public Page<Enrollment> searchEnrollmentsWithRole(
            Long courseId, Long userId, String status, String search, User currentUser, Pageable pageable) {
        
        Integer filterManagerId = null;
        
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            filterManagerId = currentUser.getId(); 
        }
        
        return enrollmentRepository.searchEnrollments(courseId, userId, status, filterManagerId, search, pageable);
    }

    // Helper for Excel Export (unpaged)
    public List<Enrollment> searchAllEnrollmentsWithRole(
            Long courseId, Long userId, String status, String search, User currentUser) {
        
        Integer filterManagerId = null;
        
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            filterManagerId = currentUser.getId(); 
        }
        
        return enrollmentRepository.searchEnrollments(courseId, userId, status, filterManagerId, search, Pageable.unpaged()).getContent();
    }

    public void updateEnrollmentStatusAndNotes(Long enrollmentId, String newStatus, String notes, User currentUser) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        if (enrollment == null) throw new RuntimeException("Không tìm thấy đơn đăng ký");

        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            if (enrollment.getCourse().getManager() == null || !enrollment.getCourse().getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn không có quyền duyệt đơn của khóa học này hoặc khóa học chưa có quản lý!");
            }
        }

        // Cập nhật dữ liệu
        enrollment.setStatus(newStatus);
        enrollment.setNotes(notes);
        enrollment.setLastUpdated(LocalDateTime.now());
        enrollment.setRejectNotes(notes);
        
        updateEnrollment(enrollment);
    }

    public void addNewEnrollmentByAdmin(Long courseId, Long userId, String status, String rejectNotes, User currentUser) {
        Course course = courseService.getCourseById(courseId);
        User user = userService.getUserById(userId.intValue()); 

        if (course == null || user == null) {
            throw new RuntimeException("Khóa học hoặc User không tồn tại!");
        }

        // Check trùng enrollment
        if (isAlreadyEnrolled(userId, courseId)) {
            throw new RuntimeException("user đã đăng kí khóa học này rồi");
        }

        // Check quyền Manager: Chỉ được thêm học viên vào khóa của mình
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            if (course.getManager() == null || !course.getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Bạn không có quyền thêm học viên vào khóa học này hoặc khóa học chưa có quản lý!");
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setUser(user);
        enrollment.setFullName(user.getFullName());
        enrollment.setEmail(user.getEmail());
        enrollment.setFee(course.getPrice());
        enrollment.setStatus(status);
        enrollment.setNotes(rejectNotes); 
        enrollment.setProgress(0.0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setRejectNotes(rejectNotes);

        enrollmentRepository.save(enrollment);
    }

    public void deleteEnrollment(Long enrollmentId, User currentUser) {
        Enrollment enrollment = getEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new RuntimeException("Enrollment not found!");
        }

        if ("MANAGER".equalsIgnoreCase(currentUser.getRole().getValue())) {
            if (enrollment.getCourse().getManager() == null || !enrollment.getCourse().getManager().getId().equals(currentUser.getId())) {
                throw new RuntimeException("You don't have permission to delete this enrollment or the course has no assigned manager!");
            }
        }

        enrollmentRepository.deleteById(enrollmentId);
    }
}
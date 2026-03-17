package org.example.assignment2.service;

import org.example.assignment2.model.User;
import org.example.assignment2.repository.CourseRepository;
import org.example.assignment2.repository.EnrollmentRepository;
import org.example.assignment2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public Map<String, Object> getDashboardStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        String roleValue = user.getRole() != null ? user.getRole().getValue() : "";
        boolean isManager = "ROLE_MANAGER".equalsIgnoreCase(roleValue)
                         || "MANAGER".equalsIgnoreCase(roleValue);

        if (isManager) {
            Integer managerId = user.getId();

            // Total users enrolled in this manager's courses (distinct)
            stats.put("totalUsers", enrollmentRepository.countDistinctUsersByManagerId(managerId));

            // Total courses managed by this manager
            stats.put("totalCourses", courseRepository.countByManager_Id(managerId));

            // Active members (Paid/Approved) in this manager's courses (distinct)
            stats.put("activeMembers", enrollmentRepository.countDistinctActiveMembersByManagerId(managerId));

            // Published courses managed by this manager
            stats.put("publishedCourses", courseRepository.countByManager_IdAndStatus(managerId, "Published"));

            // Recent courses of this manager
            stats.put("recentCourses",
                    courseRepository.findTop7ByManager_IdOrderByCreatedAtDesc(user.getId().longValue()));

        } else {
            // Admin: see everything
            stats.put("totalUsers", userRepository.count());
            stats.put("totalCourses", courseRepository.count());
            stats.put("activeMembers", userRepository.countByRoleIdAndStatus(5, "Active"));
            stats.put("publishedCourses", courseRepository.countByStatus("Published"));
            stats.put("recentCourses", courseRepository.findTop7ByOrderByCreatedAtDesc());
        }

        return stats;
    }
}

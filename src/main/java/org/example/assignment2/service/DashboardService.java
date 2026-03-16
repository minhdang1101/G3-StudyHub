package org.example.assignment2.service;

import org.example.assignment2.model.Course;
import org.example.assignment2.repository.CourseRepository;
import org.example.assignment2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("activeMembers", userRepository.countByRoleIdAndStatus(5, "Active"));
        stats.put("publishedCourses", courseRepository.countByStatus("Published"));
        stats.put("recentCourses", courseRepository.findTop7ByOrderByCreatedAtDesc());

        return stats;
    }
}

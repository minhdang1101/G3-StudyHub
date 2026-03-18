package org.example.assignment2.service;

import org.example.assignment2.model.Course;
import org.example.assignment2.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;

    // 1. Lấy danh sách tất cả khóa học
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // 2. Tìm khóa học theo ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    // 3. Thêm mới hoặc Cập nhật khóa học (BỔ SUNG)
    public void saveCourse(Course course) {
        courseRepository.save(course);
    }

    // 4. Xóa khóa học theo ID (BỔ SUNG)
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}
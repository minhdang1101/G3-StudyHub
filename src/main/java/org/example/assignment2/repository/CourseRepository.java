package org.example.assignment2.repository;

import org.example.assignment2.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Search theo title
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    List<Course> findByCategoryId(Integer categoryId);
}

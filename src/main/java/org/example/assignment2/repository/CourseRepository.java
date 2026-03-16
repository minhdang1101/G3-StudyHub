package org.example.assignment2.repository;

import org.example.assignment2.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategory_Id(Integer categoryId);

    List<Course> findByTitleContainingIgnoreCase(String keyword);

    List<Course> findByCategoryId(Integer categoryId);

    long countByStatus(String status);

    List<Course> findTop7ByOrderByCreatedAtDesc();
}


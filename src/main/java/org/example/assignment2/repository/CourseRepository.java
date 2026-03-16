package org.example.assignment2.repository;

import org.example.assignment2.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByCategory_Id(Integer categoryId);

    List<Course> findByTitleContainingIgnoreCase(String keyword);

}
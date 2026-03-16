package org.example.assignment2.repository;

import org.example.assignment2.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findTop6ByStatusOrderByCreatedAtDesc(Integer status);
    @Query("""
        SELECT c FROM Course c
        WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR c.category.id = :categoryId)
          AND (:instructorId IS NULL OR c.instructor.id = :instructorId)
          AND (:status IS NULL OR c.status = :status)
        ORDER BY c.courseId DESC
    """)
    List<Course> searchCourses(@Param("keyword") String keyword,
                               @Param("categoryId") Integer categoryId,
                               @Param("instructorId") Integer instructorId,
                               @Param("status") Integer status);
}
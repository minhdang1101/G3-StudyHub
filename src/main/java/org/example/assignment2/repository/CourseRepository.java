package org.example.assignment2.repository;

import org.example.assignment2.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategory_Id(Integer categoryId);

    List<Course> findByTitleContainingIgnoreCase(String keyword);

    long countByStatus(String status);

    List<Course> findTop7ByOrderByCreatedAtDesc();
    
    List<Course> findTop7ByManager_IdOrderByCreatedAtDesc(Long managerId);

    long countByManager_Id(Integer managerId);

    long countByManager_IdAndStatus(Integer managerId, String status);

    List<Course> findByManager_Id(Integer managerId);

    List<Course> findTop6ByStatusOrderByCreatedAtDesc(String status);

    List<Course> findByStatusOrderByCreatedAtDesc(String status);

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
                               @Param("status") String status);
}

package org.example.assignment2.repository;

import org.example.assignment2.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {


    List<Enrollment> findByUser_Id(Long userId); 

    Page<Enrollment> findByUser_IdOrderByEnrolledAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Enrollment e WHERE e.user.id = :userId AND e.course.courseId = :courseId " +
           "AND e.status NOT IN ('Cancelled', 'Rejected')")
    boolean existsActiveEnrollment(@Param("userId") Long userId, @Param("courseId") Long courseId);

    // Manager-scoped: total distinct users enrolled in manager's courses
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.manager.id = :managerId")
    long countDistinctUsersByManagerId(@Param("managerId") Integer managerId);

    // Manager-scoped: distinct active members (Paid/Approved) in manager's courses
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.manager.id = :managerId " +
           "AND e.status IN ('Paid', 'Approved')")
    long countDistinctActiveMembersByManagerId(@Param("managerId") Integer managerId);

    @Query("SELECT e FROM Enrollment e WHERE " +
           "(:courseId IS NULL OR e.course.id = :courseId) AND " +
           "(:userId IS NULL OR e.user.id = :userId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Enrollment> searchAdvanced(
            @Param("courseId") Long courseId, 
            @Param("userId") Long userId, 
            @Param("status") String status, 
            @Param("keyword") String keyword);
       
      @Query("SELECT e FROM Enrollment e WHERE " +
            "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
            "(:userId IS NULL OR e.user.id = :userId) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:managerId IS NULL OR e.course.manager.id = :managerId) AND " + 
            "(:search IS NULL OR :search = '' OR LOWER(e.course.title) LIKE LOWER(CONCAT('%', :search, '%')))")
     Page<Enrollment> searchEnrollments(
             @Param("courseId") Long courseId, 
             @Param("userId") Long userId, 
             @Param("status") String status, 
             @Param("managerId") Integer managerId, 
             @Param("search") String search,
             Pageable pageable);
     }
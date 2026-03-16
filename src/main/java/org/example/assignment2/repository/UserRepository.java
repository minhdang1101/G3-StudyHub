package org.example.assignment2.repository;

import org.example.assignment2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE " +
            "(:roleId IS NULL OR u.role.id = :roleId) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:keyword IS NULL OR u.fullName LIKE %:keyword% OR u.email LIKE %:keyword% OR u.mobile LIKE %:keyword%)")
    List<User> filterUsers(@Param("roleId") Integer roleId,
                           @Param("status") String status,
                           @Param("keyword") String keyword);

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    long countByStatus(String status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId AND u.status = :status")
    long countByRoleIdAndStatus(@Param("roleId") Integer roleId, @Param("status") String status);
}

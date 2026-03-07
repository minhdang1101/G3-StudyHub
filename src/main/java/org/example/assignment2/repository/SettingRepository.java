package org.example.assignment2.repository;

import org.example.assignment2.model.Setting;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer> {

    List<Setting> findByParentIsNull();

    @Query("SELECT s FROM Setting s WHERE s.parent.name = 'User Role' AND s.status = 'Active'")
    List<Setting> findActiveRoles();

    @Query(value = "SELECT * FROM settings s " +
            "WHERE s.type_id IS NOT NULL " +
            "AND (:parentId IS NULL OR s.type_id = :parentId) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:keyword IS NULL OR s.name ILIKE CONCAT('%', :keyword, '%') OR s.value ILIKE CONCAT('%', :keyword, '%'))",
            nativeQuery = true)
    List<Setting> filterAndSearch(@Param("parentId") Integer parentId,
                                  @Param("status") String status,
                                  @Param("keyword") String keyword,
                                  Sort sort);

    boolean existsByName(String name);


}
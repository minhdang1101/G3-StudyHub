package org.example.assignment2.repository;

import org.example.assignment2.model.Permission;
import org.example.assignment2.model.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, PermissionId> {
    public List<Permission> findByRoleId(Integer roleId);
}

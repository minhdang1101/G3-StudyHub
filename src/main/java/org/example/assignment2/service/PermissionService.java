package org.example.assignment2.service;

import org.example.assignment2.dto.PermissionDTO;
import org.example.assignment2.dto.UserDTO;
import org.example.assignment2.model.Permission;
import org.example.assignment2.model.PermissionId;
import org.example.assignment2.model.User;
import org.example.assignment2.repository.PermissionRepository;
import org.example.assignment2.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {
    @Autowired
    private UserService userService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Permission> findAll(){
        return permissionRepository.findAll();
    }

    public List<Permission> findByRoleId(Integer roleId){
        return permissionRepository.findByRoleId(roleId);
    }

    @Transactional
    public void save(PermissionDTO permissiondto){
        PermissionId id = new PermissionId();
        id.setRoleId(permissiondto.getRoleId());
        id.setPageId(permissiondto.getPageId());

        Permission permission;

        if (permissionRepository.existsById(id)) {
            permission = permissionRepository.findById(id).orElse(new Permission());
        } else {
            permission = new Permission();
            permission.setId(id);
        }

        permission.setCanRead(permissiondto.getCanRead());
        permission.setCanAdd(permissiondto.getCanAdd());
        permission.setCanEdit(permissiondto.getCanEdit());
        permission.setCanDelete(permissiondto.getCanDelete());

        permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Integer roleId, Integer pageId){
        PermissionId id = new PermissionId();
        id.setRoleId(roleId);
        id.setPageId(pageId);
        permissionRepository.deleteById(id);
    }

    public Permission getPermission(Integer roleId, Integer pageId) {

        PermissionId id = new PermissionId();
        id.setRoleId(roleId);
        id.setPageId(pageId);

        return permissionRepository.findById(id).orElse(null);
    }

    public boolean canRead(Integer roleId, Integer pageId) {
        Permission permission = getPermission(roleId, pageId);
        return permission != null && Boolean.TRUE.equals(permission.getCanRead());
    }

    public boolean canAdd(Integer roleId, Integer pageId) {
        Permission permission = getPermission(roleId, pageId);
        return permission != null && Boolean.TRUE.equals(permission.getCanAdd());
    }

    public boolean canEdit(Integer roleId, Integer pageId) {
        Permission permission = getPermission(roleId, pageId);
        return permission != null && Boolean.TRUE.equals(permission.getCanEdit());
    }

    public boolean canDelete(Integer roleId, Integer pageId) {
        Permission permission = getPermission(roleId, pageId);
        return permission != null && Boolean.TRUE.equals(permission.getCanDelete());
    }


}


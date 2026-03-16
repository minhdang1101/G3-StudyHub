package org.example.assignment2.controller;

import org.example.assignment2.dto.PermissionDTO;
import org.example.assignment2.model.Permission;
import org.example.assignment2.model.Setting;
import org.example.assignment2.service.PermissionService;
import org.example.assignment2.service.SettingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SettingService settingService;

    @GetMapping
    public String listPermissions(Model model) {

        List<Permission> permissions = permissionService.findAll();
        List<Setting> roles = settingService.getRoleSettings();
        List<Setting> pages = settingService.getPageSettings();

        model.addAttribute("permissions", permissions);
        model.addAttribute("roles", roles);
        model.addAttribute("pages", pages);

        return "permission/permission-list";
    }

    @PostMapping("/save")
    public String savePermission(@ModelAttribute PermissionDTO permissionDTO) {

        permissionService.save(permissionDTO);

        return "redirect:/permissions";
    }

    @GetMapping("/delete")
    public String deletePermission(
            @RequestParam Integer roleId,
            @RequestParam Integer pageId
    ) {

        permissionService.delete(roleId, pageId);

        return "redirect:/permissions";
    }

    @GetMapping("/role/{roleId}")
    public String permissionByRole(
            @PathVariable Integer roleId,
            Model model
    ) {

        List<Permission> permissions = permissionService.findByRoleId(roleId);
        List<Setting> roles = settingService.getRoleSettings();
        List<Setting> pages = settingService.getPageSettings();

        model.addAttribute("permissions", permissions);
        model.addAttribute("roles", roles);
        model.addAttribute("pages", pages);
        model.addAttribute("selectedRoleId", roleId);

        return "permission/permission-list";
    }

}
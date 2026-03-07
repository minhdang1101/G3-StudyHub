package org.example.assignment2.controller;

import jakarta.validation.Valid;
import org.example.assignment2.dto.SettingDTO;
import org.example.assignment2.model.Setting;
import org.example.assignment2.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;


    @GetMapping("")
    public String list(Model model,
                       @RequestParam(name = "typeId", required = false) Integer typeId,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "sortField", defaultValue = "id") String sortField,
                       @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {

        List<Setting> list = settingService.getSettings(typeId, status, keyword, sortField, sortDir);
        model.addAttribute("settings", list);
        model.addAttribute("typeList", settingService.getAllTypes());

        model.addAttribute("currentTypeId", typeId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "setting/setting-list";
    }

    // 3. Xử lý Deactivate / Activate
    @GetMapping("/status/{id}")
    public String changeStatus(@PathVariable("id") Integer id,
                               @RequestParam("status") String newStatus,
                               RedirectAttributes redirectAttributes) {
        try {
            SettingDTO dto = settingService.getSettingDtoById(id);
            dto.setStatus(newStatus);
            settingService.saveSetting(dto);

            redirectAttributes.addFlashAttribute("message", "Updated status to " + newStatus + " successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error updating status!");
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/settings";
    }

    // 4. Màn hình thêm mới (Add New)
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("setting", new SettingDTO()); // DTO rỗng
        model.addAttribute("typeList", settingService.getAllTypes()); // Dropdown Type
        return "setting/setting-detail";
    }

    // 5. Màn hình chỉnh sửa (Edit)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        try {
            SettingDTO dto = settingService.getSettingDtoById(id);
            model.addAttribute("setting", dto);
            model.addAttribute("typeList", settingService.getAllTypes());
            return "setting/setting-detail";
        } catch (RuntimeException e) {
            return "redirect:/settings";
        }
    }

    @PostMapping("/save")
    public String saveSetting(@Valid @ModelAttribute("setting") SettingDTO settingDto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("typeList", settingService.getAllTypes());
            return "setting/setting-detail";
        }

        try {
            settingService.saveSetting(settingDto);
            redirectAttributes.addFlashAttribute("message", "Saved setting successfully!");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Error saving setting: " + e.getMessage());
            model.addAttribute("typeList", settingService.getAllTypes());
            return "setting/setting-detail";
        }

        return "redirect:/settings";
    }
}
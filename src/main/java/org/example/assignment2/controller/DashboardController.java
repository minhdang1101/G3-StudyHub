package org.example.assignment2.controller;

import org.example.assignment2.model.User;
import org.example.assignment2.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // Redirect Members to public courses page
        String roleValue = user.getRole() != null ? user.getRole().getValue() : "";
        if ("ROLE_MEMBER".equalsIgnoreCase(roleValue)) {
            return "redirect:/courses";
        }

        Map<String, Object> stats = dashboardService.getDashboardStats(user);
        model.addAllAttributes(stats);
        model.addAttribute("currentUser", user);
        return "dashboard";
    }
}

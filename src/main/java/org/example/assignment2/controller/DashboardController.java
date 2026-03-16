    package org.example.assignment2.controller;

    import org.example.assignment2.model.User;
import org.example.assignment2.service.DashboardService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

    import java.util.Map;



    @Controller
    public class DashboardController {

        @Autowired
        private DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
public String showDashboard(Model model, HttpSession session, @RequestParam(required = false) String role) {
    if (session.getAttribute("user") == null) {
        User mockUser = new User();
        org.example.assignment2.model.Setting mockRole = new org.example.assignment2.model.Setting();
        
        if ("manager".equalsIgnoreCase(role)) {
            mockUser.setId(2); 
            mockUser.setFullName("Nguyễn Quản Lý (Mock)");
            mockRole.setId(8); 
            mockRole.setValue("ROLE_MANAGER");
        } else {
            mockUser.setId(1);
            mockUser.setFullName("Hệ thống Admin (Mock)");
            mockRole.setId(3); 
            mockRole.setValue("ROLE_ADMIN");
        }
        mockUser.setRole(mockRole);
        session.setAttribute("user", mockUser);
    }
    User user = (User) session.getAttribute("user");
    Map<String, Object> stats = dashboardService.getDashboardStats(user);
    model.addAllAttributes(stats);
    model.addAttribute("currentUser", user);
    return "dashboard";
}
}


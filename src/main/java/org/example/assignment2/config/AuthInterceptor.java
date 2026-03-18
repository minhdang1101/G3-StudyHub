package org.example.assignment2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.assignment2.model.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String roleValue = user.getRole() != null ? user.getRole().getValue() : "";
        
        // Block MEMBERS from accessing admin/manager pages
        if ("ROLE_MEMBER".equalsIgnoreCase(roleValue) || "MEMBER".equalsIgnoreCase(roleValue)) {
            FlashMap flashMap = new FlashMap();
            flashMap.put("errorMessage", "Bạn không có quyền truy cập vào chức năng này.");
            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
            if (flashMapManager != null) {
                flashMapManager.saveOutputFlashMap(flashMap, request, response);
            }
            response.sendRedirect(request.getContextPath() + "/courses");
            return false;
        }

        // Allow ADMIN and MANAGER
        return true;
    }
}

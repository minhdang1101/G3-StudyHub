package org.example.assignment2.controller;

import jakarta.servlet.http.HttpSession;
import org.example.assignment2.model.User;
import org.example.assignment2.model.Permission;
import org.example.assignment2.service.EmailService;
import org.example.assignment2.service.UserService;
import org.example.assignment2.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ){

        User user = userService.login(email,password);

        if(user == null){
            model.addAttribute("error","Invalid email or password");
            return "login";
        }

        Integer roleId = user.getRole().getId();

        List<Permission> permissions = permissionService.findByRoleId(roleId);

        session.setAttribute("user",user);
        session.setAttribute("roleId",roleId);
        session.setAttribute("permissions",permissions);

        if(roleId == 1){
            return "redirect:/dashboard";
        }else return "redirect:/homepage";

    }
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(){
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword){

        userService.resetPassword(email,newPassword);

        return "redirect:/login?reset=success";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email, HttpSession session){

        if(userService.findByEmail(email)==null){
            return "redirect:/forgot-password?error=email";
        }

        String otp = String.valueOf(new Random().nextInt(900000)+100000);

        session.setAttribute("otp",otp);
        session.setAttribute("email",email);

        emailService.sendOtp(email,otp);

        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,HttpSession session){

        String sessionOtp=(String) session.getAttribute("otp");

        if(sessionOtp.equals(otp)){
            return "reset-password";
        }

        return "verify-otp?error=otp";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password,
                                HttpSession session){

        String email=(String) session.getAttribute("email");

        userService.resetPassword(email,password);

        session.removeAttribute("otp");

        return "redirect:/login?reset=success";
    }


}

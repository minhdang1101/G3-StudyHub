package org.example.assignment2.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.assignment2.dto.UserDTO;
import org.example.assignment2.model.User;
import org.example.assignment2.repository.CommentRepository;
import org.example.assignment2.repository.PostRepository;
import org.example.assignment2.repository.SettingRepository;
import org.example.assignment2.service.EmailService;
import org.example.assignment2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private SettingRepository settingRepo;
    @Autowired
    private PostRepository postRepo;
    @Autowired
    private CommentRepository commentRepo;
    @Autowired
    private EmailService emailService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(name = "roleId", required = false) Integer roleId,
                       @RequestParam(name = "status", required = false) String status,
                       @RequestParam(name = "keyword", required = false) String keyword) {

        model.addAttribute("users", userService.getUsers(roleId, status, keyword));
        model.addAttribute("roleList", settingRepo.findActiveRoles());
        model.addAttribute("currentRoleId", roleId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);

        if (!model.containsAttribute("newUser")) {
            model.addAttribute("newUser", new UserDTO());
        }

        return "user/user-list";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("newUser") UserDTO userDto,
                       BindingResult result,
                       Model model) {

        if (userService.isEmailExists(userDto.getEmail())) {
            result.rejectValue("email", "error.email", "Email này đã tồn tại!");
        }

        if (result.hasErrors()) {
            model.addAttribute("showModal", true);
            return list(model, null, null, null);
        }
        userDto.setStatus("Unverified");
        userDto.setRoleId(3);
        String randomPassword = UUID.randomUUID().toString().substring(0, 8);

        userDto.setPassword(randomPassword);

        emailService.sendAccountEmail(userDto.getEmail(), randomPassword);
        userService.saveUser(userDto);
        return "redirect:/users";
    }

    @GetMapping("/approve/{id}")
    public String approveUser(@PathVariable("id") Integer id) {
        userService.updateUserStatus(id, "Active");
        return "redirect:/users";
    }

    @GetMapping("/block/{id}")
    public String blockUser(@PathVariable("id") Integer id) {
        userService.updateUserStatus(id, "Blocked");
        return "redirect:/users";
    }

    @GetMapping("/unblock/{id}")
    public String unblockUser(@PathVariable("id") Integer id) {
        userService.updateUserStatus(id, "Active");
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/users";
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setStatus(user.getStatus());
        dto.setAvatarUrl(user.getAvatarUrl());

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
        }

        model.addAttribute("userDto", dto);
        model.addAttribute("roleList", settingRepo.findActiveRoles());

        return "user/user-detail";
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("userDto") UserDTO userDto,
                             BindingResult result,
                             @RequestParam(value = "imageFile", required = false) MultipartFile file,
                             Model model,
                             HttpServletRequest request) {
        User existingUser = userService.findByEmail(userDto.getEmail());
        if (existingUser != null && !existingUser.getId().equals(userDto.getId())) {
            result.rejectValue("email", "error.email", "Email đã được sử dụng bởi người khác!");
        }

        if (result.hasErrors()) {
            model.addAttribute("roleList", settingRepo.findActiveRoles());
            return "user/user-detail";
        }
        if (!file.isEmpty()) {
            String avatarPath = saveFile(file, request);
            if (avatarPath != null) {
                userDto.setAvatarUrl(avatarPath);
            }
        } else {
            User oldUser = userService.getUserById(userDto.getId());
            if (oldUser != null) {
                userDto.setAvatarUrl(oldUser.getAvatarUrl());
            }
        }

        userService.saveUser(userDto);
        return "redirect:/users";
    }

    private String saveFile(MultipartFile file, HttpServletRequest request) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            String uploadDir = request.getServletContext().getRealPath("/uploads/");

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            return "uploads/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/records/{id}")
    public String showUserRecords(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) return "redirect:/users";

        model.addAttribute("user", user);

        // Thống kê số liệu [cite: 196, 197]
        model.addAttribute("postCount", postRepo.countByAuthorId(id));
        model.addAttribute("commentCount", commentRepo.countByUserId(id));

        // Danh sách bài viết và comment [cite: 198, 202]
        model.addAttribute("posts", postRepo.findByAuthorId(id));
        model.addAttribute("comments", commentRepo.findByUserId(id));

        return "user/user-records";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }
}
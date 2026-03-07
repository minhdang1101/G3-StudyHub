package org.example.assignment2.service;

import org.example.assignment2.dto.UserDTO;
import org.example.assignment2.model.Setting;
import org.example.assignment2.model.User;
import org.example.assignment2.repository.SettingRepository;
import org.example.assignment2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private SettingRepository settingRepo;

    public List<User> getUsers(Integer roleId, String status, String keyword) {
        String key = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String stat = (status != null && !status.trim().isEmpty()) ? status : null;
        return userRepo.filterUsers(roleId, stat, key);
    }

    @Transactional
    public void saveUser(UserDTO userDto) {
        User user;
        if (userDto.getId() != null) {
            user = userRepo.findById(userDto.getId())
                    .orElse(new User());
        } else {
            user = new User();
        }

        user.setFullName(userDto.getFullName());
        user.setEmail(userDto.getEmail());
        user.setMobile(userDto.getMobile());
        user.setNote(userDto.getNote());
        user.setStatus(userDto.getStatus());
        user.setAvatar(userDto.getAvatar());

        // Xử lý Role (nếu có thay đổi)
        if (userDto.getRoleId() != null) {
            Setting role = settingRepo.findById(userDto.getRoleId()).orElse(null);
            user.setRole(role);
        }
        userRepo.save(user);
    }

    public void updateUserStatus(Integer userId, String newStatus) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(newStatus);
        userRepo.save(user);
    }

    public boolean isEmailExists(String email) {
        return userRepo.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }

    public User getUserById(Integer id) {
        return userRepo.findById(id).orElse(null);
    }
}
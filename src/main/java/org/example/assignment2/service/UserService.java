package org.example.assignment2.service;

import org.example.assignment2.dto.RegisterDTO;
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
        user.setStatus(userDto.getStatus());
        user.setUsername(userDto.getUsername());
        user.setAvatarUrl(userDto.getAvatarUrl());

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

    @Transactional
    public void saveUser(User user) {
        if (user != null) {
            userRepo.save(user);
        }
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

    public User login(String email,String password){

        User user = userRepo.findByEmail(email).orElse(null);

        if(user == null){
            return null;
        }

        if(!user.getPassword().equals(password)){
            return null;
        }

        return user;
    }

    public void resetPassword(String email,String password){

        User user = userRepo.findByEmail(email).orElse(null);

        if(user != null){
            user.setPassword(password);
            userRepo.save(user);
        }
    }

    public void register(RegisterDTO dto) {

        User user = new User();

        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());

        user.setStatus("ACTIVE");

        Setting role = settingRepo.findById(3).orElse(null);
        user.setRole(role);

        userRepo.save(user);
    }



    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
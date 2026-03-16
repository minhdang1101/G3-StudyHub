package org.example.assignment2.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String fullName;

    private String username;

    private String email;

    private String password;

    private String confirmPassword;

    private String mobile;
}

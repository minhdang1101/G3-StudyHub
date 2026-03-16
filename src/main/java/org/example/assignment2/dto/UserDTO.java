package org.example.assignment2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    private Integer id;

    @NotEmpty(message = "Full name is required")
    private String fullName;

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobile;

    @NotNull(message = "Role is required")
    private Integer roleId;

    private String status;
    private String avatar;
    private String note;
    private String password;
    private String confirmPassword;
}
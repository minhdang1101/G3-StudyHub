package org.example.assignment2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Date;


@Data
public class UserDTO {
    private Integer id;

    @NotEmpty(message = "Full name is required")
    private String fullName;

    @NotEmpty
    private String username;

    private String password;

    @NotEmpty(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String mobile;

    private Integer roleId;

    private String status;
    private String avatarUrl;
    private String note;
    private String confirmPassword;

    private Date createdAt;
    private Date lastLogin;
}
package org.example.assignment2.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;
    private String otp;
    private String newPassword;
}
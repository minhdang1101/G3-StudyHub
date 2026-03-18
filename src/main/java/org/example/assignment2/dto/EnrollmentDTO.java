package org.example.assignment2.dto;

import lombok.Data;

@Data
public class EnrollmentDTO {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private Double totalFee;
    private String fullName;
    private String email;
    private String mobile;
    private String paymentMethod;
    private String enrollNote;
    private String notes;
}

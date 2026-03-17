package org.example.assignment2.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseDTO {

    private Integer courseId;

    @NotEmpty(message = "Title is required")
    private String title;

    private String description;

    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Level is required")
    private Integer levelId;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    @NotNull(message = "Instructor is required")
    private Integer instructorId;

    @NotNull(message = "Duration is required")
    @Min(value = 0, message = "Duration must be greater than or equal to 0")
    private Integer durationHours;

    @NotNull(message = "Status is required")
    private Integer status;
}
package org.example.assignment2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChapterDTO {

    private Integer chapterId;

    @NotNull(message = "Course is required")
    private Long courseId;

    @NotEmpty(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex;

    @NotNull(message = "Status is required")
    private Integer status;
}
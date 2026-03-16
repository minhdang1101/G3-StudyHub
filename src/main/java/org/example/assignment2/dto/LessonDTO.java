package org.example.assignment2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonDTO {

    private Integer lessonId;

    @NotNull(message = "Chapter is required")
    private Integer chapterId;

    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Content type is required")
    private String contentType;

    private String contentUrl;

    @NotNull(message = "Duration is required")
    @Min(value = 0, message = "Duration must be greater than or equal to 0")
    private Integer durationMinutes;

    @NotNull(message = "Order index is required")
    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex;

    @NotNull(message = "Preview flag is required")
    private Boolean isPreview;

    @NotNull(message = "Status is required")
    private Integer status;
}
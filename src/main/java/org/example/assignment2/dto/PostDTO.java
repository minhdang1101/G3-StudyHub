package org.example.assignment2.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class PostDTO {
    private Integer id;

    @NotEmpty(message = "Title is required")
    private String title;

    @NotEmpty(message = "Content is required")
    private String content;

    @NotEmpty(message = "Status is required")
    private String status;

    @NotNull(message = "Author is required")
    private Integer authorId;

    private String thumbnailUrl;
}

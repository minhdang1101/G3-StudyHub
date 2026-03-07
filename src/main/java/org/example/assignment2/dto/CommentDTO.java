package org.example.assignment2.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentDTO {
    private Integer id;

    @NotEmpty(message = "Content is required")
    private String comment;

    @NotNull(message = "Post ID is required")
    private Integer postId;

    @NotNull(message = "User ID is required")
    private Integer userId;
}

package org.example.assignment2.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PermissionDTO {
    @NotNull(message = "Role is required")
    private Integer roleId;

    @NotNull(message = "Page is required")
    private Integer pageId;

    private Boolean canRead;

    private Boolean canAdd;

    private Boolean canEdit;

    private Boolean canDelete;
}

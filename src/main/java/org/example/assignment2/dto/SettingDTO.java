package org.example.assignment2.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettingDTO {
    private Integer id;
    @NotNull(message = "Type cannot be empty")
    private Integer typeId;
    @NotNull(message = "Name cannot be empty")
    private String name;
    @NotNull(message = "Value cannot be empty")
    private String value;
    @NotNull(message = "Order cannot be empty")
    @Min(value = 1, message = "Order index must be a positive number (min 1)")
    private Integer orderIndex;
    @NotNull(message = "Status cannot be empty")
    private String status;
    private String description;
}
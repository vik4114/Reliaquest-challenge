package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeCreateRequest {

    @NotBlank(message = "Name field cannot be empty")
    private String name;

    @NotNull(message = "Salary must be provided") @Positive(message = "Salary must be greater than zero") private Integer salary;

    @NotNull(message = "Age is required") @Min(value = 16, message = "Age should not be less than 16")
    @Max(value = 75, message = "Age should not exceed 75")
    private Integer age;

    @NotBlank(message = "Title is required")
    private String title;
}

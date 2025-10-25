package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class EmployeeDTO {
    private String id;

    @JsonAlias("employee_name")
    private String name;

    @JsonAlias("employee_salary")
    private Integer salary;

    @JsonAlias("employee_age")
    private Integer age;

    @JsonAlias("employee_title")
    private String title;

    @JsonAlias("employee_email")
    private String email;
}

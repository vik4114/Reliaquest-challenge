package com.reliaquest.api.model;

import com.reliaquest.api.dto.EmployeeDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeeResponse extends ApiResponse<EmployeeDTO> {}

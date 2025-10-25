package com.reliaquest.api.model;

import com.reliaquest.api.dto.EmployeeDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeeListResponse extends ApiResponse<List<EmployeeDTO>> {}

package com.reliaquest.api.model;

import com.reliaquest.api.dto.EmployeeDTO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EmployeeListResponse extends ApiResponse<List<EmployeeDTO>> {}

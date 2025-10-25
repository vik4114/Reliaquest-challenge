package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {

    List<EmployeeDTO> fetchAll();

    EmployeeDTO fetchById(String id);

    List<EmployeeDTO> searchByName(String name);

    List<String> getTopTenEmployeeNamesBySalary();

    int getHighestSalary();

    EmployeeDTO create(EmployeeCreateRequest createRequest);

    String deleteById(String id);
}

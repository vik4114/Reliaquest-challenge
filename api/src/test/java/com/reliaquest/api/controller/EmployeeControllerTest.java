package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO employee1;
    private EmployeeDTO employee2;

    @BeforeEach
    void setUp() {
        employee1 = new EmployeeDTO();
        employee1.setId("1");
        employee1.setName("John Doe");
        employee1.setSalary(50000);
        employee1.setAge(30);
        employee1.setTitle("Developer");
        employee1.setEmail("john@example.com");

        employee2 = new EmployeeDTO();
        employee2.setId("2");
        employee2.setName("Jane Smith");
        employee2.setSalary(60000);
        employee2.setAge(28);
        employee2.setTitle("Manager");
        employee2.setEmail("jane@example.com");
    }

    @Test
    void getAllEmployees_shouldReturnListOfEmployees() throws Exception {
        List<EmployeeDTO> employees = Arrays.asList(employee1, employee2);
        Mockito.when(employeeService.fetchAll()).thenReturn(employees);
        mockMvc.perform(get("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnMatchingEmployees() throws Exception {
        Mockito.when(employeeService.searchByName("John")).thenReturn(Collections.singletonList(employee1));
        mockMvc.perform(get("/api/v1/employee/search/John")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        Mockito.when(employeeService.fetchById("1")).thenReturn(employee1);
        mockMvc.perform(get("/api/v1/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary() throws Exception {
        Mockito.when(employeeService.getHighestSalary()).thenReturn(90000);
        mockMvc.perform(get("/api/v1/employee/highestSalary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("90000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnNames() throws Exception {
        List<String> names = Arrays.asList("John Doe", "Jane Smith");
        Mockito.when(employeeService.getTopTenEmployeeNamesBySalary()).thenReturn(names);
        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("John Doe"))
                .andExpect(jsonPath("$[1]").value("Jane Smith"));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("John Doe");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Developer");

        EmployeeDTO employee = new EmployeeDTO();
        employee.setId("1");
        employee.setName("John Doe");
        employee.setSalary(50000);
        employee.setAge(30);
        employee.setTitle("Developer");
        employee.setEmail(null);

        Mockito.when(employeeService.create(any(EmployeeCreateRequest.class))).thenReturn(employee);

        String json = "{" +
                "\"name\":\"John Doe\"," +
                "\"salary\":50000," +
                "\"age\":30," +
                "\"title\":\"Developer\"}";

        mockMvc.perform(post("/api/v1/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void deleteEmployeeById_shouldReturnSuccessMessage() throws Exception {
        Mockito.when(employeeService.deleteById("1")).thenReturn("Employee deleted");
        mockMvc.perform(delete("/api/v1/employee/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Employee deleted"));
    }
}


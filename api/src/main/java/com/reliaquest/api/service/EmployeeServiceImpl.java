package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.GenericResponse;
import com.reliaquest.api.utils.Retry;
import com.reliaquest.api.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.reliaquest.api.constants.Constants.EMPLOYEE;
import static com.reliaquest.api.constants.Constants.EMPLOYEE_BY_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final WebClient webClient;
    private final Utils utils;

    @Value("${server.api.url:http://localhost:8112/api/v1}")
    private String serverBaseUrl;

    @Cacheable("employeesAll")
    @Retry
    @Override
    public List<EmployeeDTO> fetchAll() {
        log.info("Fetching all employees from Mock Employee API");

        String url = serverBaseUrl + EMPLOYEE;
        WebClient.ResponseSpec responseSpec = utils.addExceptionHandling(
                webClient.get().uri(url).retrieve()
        );

        ResponseEntity<EmployeeListResponse> response = responseSpec
                .toEntity(EmployeeListResponse.class)
                .block();

        if (response == null || response.getBody() == null) {
            log.warn("Empty response received while fetching all employees");
            return Collections.emptyList();
        }

        List<EmployeeDTO> employees = response.getBody().getData();
        log.info("Successfully fetched {} employees", employees.size());
        return employees;
    }

    @Cacheable(value = "employeeById", key = "#id")
    @Retry
    @Override
    public EmployeeDTO fetchById(String id) {
        log.info("Fetching employee by ID: {}", id);

        String url = serverBaseUrl + EMPLOYEE_BY_ID.replace(":id", id);
        WebClient.ResponseSpec responseSpec = utils.addExceptionHandling(
                webClient.get().uri(url).retrieve()
        );

        ResponseEntity<EmployeeResponse> response = responseSpec
                .toEntity(EmployeeResponse.class)
                .block();

        if (response != null && response.getBody() != null) {
            log.info("Employee found with ID: {}", id);
            return response.getBody().getData();
        }

        log.error("Employee not found for ID: {}", id);
        throw new ApiException("Employee not found for id " + id, HttpStatus.NOT_FOUND);
    }

    @Cacheable(value = "searchByName", key = "#name")
    @Retry
    @Override
    public List<EmployeeDTO> searchByName(String name) {
        log.info("Searching employees containing name: {}", name);

        EmployeeService self = (EmployeeService) AopContext.currentProxy();
        List<EmployeeDTO> matchedEmployees = self.fetchAll().stream()
                .filter(e -> e.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

        log.info("Found {} employees matching name '{}'", matchedEmployees.size(), name);
        return matchedEmployees;
    }

    @Cacheable("topTenNamesBySalary")
    @Retry
    @Override
    public List<String> getTopTenEmployeeNamesBySalary() {
        log.info("Fetching top 10 employees by salary");

        EmployeeService self = (EmployeeService) AopContext.currentProxy();
        List<String> topTenEmployees = self.fetchAll().stream()
                .sorted(Comparator.comparingInt(EmployeeDTO::getSalary).reversed())
                .limit(10)
                .map(EmployeeDTO::getName)
                .toList();

        log.info("Top 10 highest earning employees: {}", topTenEmployees);
        return topTenEmployees;
    }

    @Cacheable("highestSalary")
    @Retry
    @Override
    public int getHighestSalary() {
        log.info("Fetching highest salary among employees");

        EmployeeService self = (EmployeeService) AopContext.currentProxy();
        return self.fetchAll().stream()
                .map(EmployeeDTO::getSalary)
                .max(Integer::compareTo)
                .orElse(0);
    }

    @CacheEvict(
            value = {"employeesAll", "employeeById", "searchByName", "highestSalary", "topTenNamesBySalary"},
            allEntries = true
    )
    @Retry
    @Override
    public EmployeeDTO create(EmployeeCreateRequest createRequest) {
        log.info("Creating new employee: {}", createRequest.getName());

        String url = serverBaseUrl + EMPLOYEE;
        WebClient.ResponseSpec responseSpec = utils.addExceptionHandling(
                webClient.post()
                        .uri(url)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(createRequest)
                        .retrieve()
        );

        ResponseEntity<EmployeeResponse> response = responseSpec
                .toEntity(EmployeeResponse.class)
                .block();

        if (response != null && response.getBody() != null) {
            EmployeeDTO created = response.getBody().getData();
            log.info("Employee created successfully: {}", created.getName());
            return created;
        }

        log.error("Failed to create employee: {}", createRequest.getName());
        throw new ApiException("Failed to create employee", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @CacheEvict(
            value = {"employeesAll", "employeeById", "searchByName", "highestSalary", "topTenNamesBySalary"},
            allEntries = true
    )
    @Retry
    @Override
    public String deleteById(String id) {
        log.info("Deleting employee by ID: {}", id);

        EmployeeService self = (EmployeeService) AopContext.currentProxy();
        EmployeeDTO employee = self.fetchById(id);
        String name = employee.getName();

        String url = serverBaseUrl + EMPLOYEE;
        WebClient.ResponseSpec responseSpec = utils.addExceptionHandling(
                webClient.method(HttpMethod.DELETE)
                        .uri(url)
                        .bodyValue(Map.of("name", name))
                        .retrieve()
        );

        ResponseEntity<GenericResponse> response = responseSpec
                .toEntity(GenericResponse.class)
                .block();

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            ApiResponse body = response.getBody();
            if (body != null && Boolean.parseBoolean(String.valueOf(body.getData()))) {
                log.info("Successfully deleted employee: {} (ID: {})", name, id);
                return name;
            }
        }

        log.error("Failed to delete employee with ID: {}", id);
        throw new ApiException("Failed to delete employee with id " + id,
                response != null ? HttpStatus.valueOf(response.getStatusCode().value()) : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


package com.reliaquest.api.controller;

import com.reliaquest.api.constants.ApiDocsConstant;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@Slf4j
public class EmployeeController implements IEmployeeController<EmployeeDTO, EmployeeCreateRequest> {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    @GetMapping
    @Operation(
            summary = ApiDocsConstant.GET_ALL_EMPLOYEES_SUMMARY,
            description = ApiDocsConstant.GET_ALL_EMPLOYEES_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeDTO.class))))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        log.info("Received request: getAllEmployees");

        List<EmployeeDTO> employeeDTOList = employeeService.fetchAll();
        log.debug("Fetched {} employees", employeeDTOList.size());
        return ResponseEntity.ok(employeeDTOList);
    }

    @Override
    @GetMapping("/search/{name}")
    @Operation(
            summary = ApiDocsConstant.SEARCH_EMPLOYEES_BY_NAME_SUMMARY,
            description = ApiDocsConstant.SEARCH_EMPLOYEES_BY_NAME_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeDTO.class))))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(@PathVariable String name) {
        log.info("Received request: getEmployeesByNameSearch with name={}", name);

        List<EmployeeDTO> employeeDTOList = employeeService.searchByName(name);
        log.debug("Found {} employees matching name '{}'", employeeDTOList.size(), name);
        return ResponseEntity.ok(employeeDTOList);
    }

    @Override
    @GetMapping("/{id}")
    @Operation(
            summary = ApiDocsConstant.GET_EMPLOYEE_BY_ID_SUMMARY,
            description = ApiDocsConstant.GET_EMPLOYEE_BY_ID_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDTO.class)))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Employee Not Found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String id) {
        log.info("Received request: getEmployeeById with id={}", id);

        EmployeeDTO employeeDTO = employeeService.fetchById(id);
        log.debug("Fetched employee: {}", employeeDTO);
        return ResponseEntity.ok(employeeDTO);
    }

    @Override
    @GetMapping("/highestSalary")
    @Operation(
            summary = ApiDocsConstant.GET_HIGHEST_SALARY_SUMMARY,
            description = ApiDocsConstant.GET_HIGHEST_SALARY_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("Received request: getHighestSalaryOfEmployees");

        Integer highestSalary = employeeService.getHighestSalary();
        log.debug("Highest salary among employees: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    @Operation(
            summary = ApiDocsConstant.GET_TOP_10_HIGHEST_EARNING_EMPLOYEES_SUMMARY,
            description = ApiDocsConstant.GET_TOP_10_HIGHEST_EARNING_EMPLOYEES_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Success",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("Received request: getTopTenHighestEarningEmployeeNames");

        List<String> topTenEmployeeNames = employeeService.getTopTenEmployeeNamesBySalary();
        log.debug("Top 10 highest earning employee names: {}", topTenEmployeeNames);
        return ResponseEntity.ok(topTenEmployeeNames);
    }

    @Override
    @PostMapping
    @Operation(
            summary = ApiDocsConstant.CREATE_EMPLOYEE_SUMMARY,
            description = ApiDocsConstant.CREATE_EMPLOYEE_DESCRIPTION,
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Employee creation request payload",
                            required = true,
                            content =
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = EmployeeCreateRequest.class))))
    @ApiResponse(
            responseCode = "201",
            description = "Employee Created Successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Validated @RequestBody EmployeeCreateRequest employeeCreateRequest) {
        log.info("Received request: createEmployee with payload={}", employeeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(employeeCreateRequest));
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(
            summary = ApiDocsConstant.DELETE_AN_EMPLOYEE_BY_ID_SUMMARY,
            description = ApiDocsConstant.DELETES_AN_EMPLOYEE_BY_ID_DESCRIPTION)
    @ApiResponse(
            responseCode = "200",
            description = "Employee Deleted Successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Employee Not Found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiException.class)))
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Received request: deleteEmployeeById with id={}", id);
        return ResponseEntity.ok(employeeService.deleteById(id));
    }
}

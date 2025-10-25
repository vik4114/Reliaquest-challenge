package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.ApiException;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.GenericResponse;
import com.reliaquest.api.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.AopContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private Utils utils;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<ResponseEntity<EmployeeListResponse>> employeeListResponseMono;

    @Mock
    private Mono<ResponseEntity<EmployeeResponse>> employeeResponseMono;

    @Mock
    private Mono<ResponseEntity<GenericResponse>> genericResponseMono;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "serverBaseUrl", "http://localhost:8112/api/v1");
    }

    @Test
    void testFetchAll_ReturnsEmployees() {
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "Alice", 50000),
                createEmployee("2", "Bob", 60000)
        );
        EmployeeListResponse listResponse = new EmployeeListResponse();
        listResponse.setData(employees);
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(listResponse, HttpStatus.OK);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(employeeListResponseMono);
        when(employeeListResponseMono.block()).thenReturn(responseEntity);

        List<EmployeeDTO> result = employeeService.fetchAll();
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Bob", result.get(1).getName());
    }

    @Test
    void testFetchAll_ReturnsEmptyListOnNullResponse() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(employeeListResponseMono);
        when(employeeListResponseMono.block()).thenReturn(null);

        List<EmployeeDTO> result = employeeService.fetchAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchAll_ReturnsEmptyListOnNullBody() {
        ResponseEntity<EmployeeListResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeListResponse.class)).thenReturn(employeeListResponseMono);
        when(employeeListResponseMono.block()).thenReturn(responseEntity);

        List<EmployeeDTO> result = employeeService.fetchAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void testFetchById_ReturnsEmployee() {
        String id = "1";
        EmployeeDTO employee = createEmployee(id, "Alice", 50000);
        EmployeeResponse empResponse = new EmployeeResponse();
        empResponse.setData(employee);
        ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(empResponse, HttpStatus.OK);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(employeeResponseMono);
        when(employeeResponseMono.block()).thenReturn(responseEntity);

        EmployeeDTO result = employeeService.fetchById(id);
        assertEquals("Alice", result.getName());
        assertEquals(id, result.getId());
    }

    @Test
    void testFetchById_ThrowsApiExceptionOnNotFound() {
        String id = "999";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(employeeResponseMono);
        when(employeeResponseMono.block()).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.fetchById(id));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Employee not found for id " + id));
    }

    @Test
    void testSearchByName_ReturnsMatchingEmployees() {
        EmployeeServiceImpl spyService = spy(employeeService);
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "Alice", 50000),
                createEmployee("2", "Bob", 60000),
                createEmployee("3", "Alicia", 55000)
        );

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employees).when(spyService).fetchAll();

            List<EmployeeDTO> result = spyService.searchByName("ali");

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alice")));
            assertTrue(result.stream().anyMatch(e -> e.getName().equals("Alicia")));
        }
    }

    @Test
    void testSearchByName_CaseInsensitive() {
        EmployeeServiceImpl spyService = spy(employeeService);
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "ALICE", 50000),
                createEmployee("2", "bob", 60000)
        );

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employees).when(spyService).fetchAll();

            List<EmployeeDTO> result = spyService.searchByName("alice");

            assertEquals(1, result.size());
            assertEquals("ALICE", result.get(0).getName());
        }
    }

    @Test
    void testGetTopTenEmployeeNamesBySalary_ReturnsTopTen() {
        EmployeeServiceImpl spyService = spy(employeeService);
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "A", 100000),
                createEmployee("2", "B", 200000),
                createEmployee("3", "C", 300000),
                createEmployee("4", "D", 400000),
                createEmployee("5", "E", 500000),
                createEmployee("6", "F", 600000),
                createEmployee("7", "G", 700000),
                createEmployee("8", "H", 800000),
                createEmployee("9", "I", 900000),
                createEmployee("10", "J", 1000000),
                createEmployee("11", "K", 1100000),
                createEmployee("12", "L", 1200000)
        );

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employees).when(spyService).fetchAll();

            List<String> result = spyService.getTopTenEmployeeNamesBySalary();

            assertEquals(10, result.size());
            assertEquals("L", result.get(0)); // Highest salary
            assertEquals("C", result.get(9)); // 10th highest salary
        }
    }

    @Test
    void testGetTopTenEmployeeNamesBySalary_LessThanTenEmployees() {
        EmployeeServiceImpl spyService = spy(employeeService);
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "Alice", 50000),
                createEmployee("2", "Bob", 60000),
                createEmployee("3", "Charlie", 70000)
        );

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employees).when(spyService).fetchAll();

            List<String> result = spyService.getTopTenEmployeeNamesBySalary();

            assertEquals(3, result.size());
            assertEquals("Charlie", result.get(0));
            assertEquals("Alice", result.get(2));
        }
    }

    @Test
    void testGetHighestSalary_ReturnsMaxSalary() {
        EmployeeServiceImpl spyService = spy(employeeService);
        List<EmployeeDTO> employees = Arrays.asList(
                createEmployee("1", "Alice", 50000),
                createEmployee("2", "Bob", 100000),
                createEmployee("3", "Charlie", 75000)
        );

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employees).when(spyService).fetchAll();

            int result = spyService.getHighestSalary();

            assertEquals(100000, result);
        }
    }

    @Test
    void testGetHighestSalary_ReturnsZeroForEmptyList() {
        EmployeeServiceImpl spyService = spy(employeeService);

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(Collections.emptyList()).when(spyService).fetchAll();

            int result = spyService.getHighestSalary();

            assertEquals(0, result);
        }
    }

    @Test
    void testCreate_ReturnsCreatedEmployee() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("Alice");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Engineer");

        EmployeeDTO createdEmployee = createEmployee("1", "Alice", 50000);
        EmployeeResponse empResponse = new EmployeeResponse();
        empResponse.setData(createdEmployee);
        ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(empResponse, HttpStatus.OK);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(request)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(employeeResponseMono);
        when(employeeResponseMono.block()).thenReturn(responseEntity);

        EmployeeDTO result = employeeService.create(request);
        assertEquals("Alice", result.getName());
        assertEquals(50000, result.getSalary());
    }

    @Test
    void testCreate_ThrowsApiExceptionOnFailure() {
        EmployeeCreateRequest request = new EmployeeCreateRequest();
        request.setName("Alice");
        request.setSalary(50000);
        request.setAge(30);
        request.setTitle("Engineer");

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(request)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
        when(responseSpec.toEntity(EmployeeResponse.class)).thenReturn(employeeResponseMono);
        when(employeeResponseMono.block()).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.create(request));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertTrue(exception.getMessage().contains("Failed to create employee"));
    }

    @Test
    void testDeleteById_ReturnsEmployeeName() {
        String id = "1";
        EmployeeServiceImpl spyService = spy(employeeService);
        EmployeeDTO employee = createEmployee(id, "Alice", 50000);

        GenericResponse genResponse = new GenericResponse();
        genResponse.setData("true");
        ResponseEntity<GenericResponse> responseEntity = new ResponseEntity<>(genResponse, HttpStatus.OK);

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employee).when(spyService).fetchById(id);

            when(webClient.method(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any(Map.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
            when(responseSpec.toEntity(GenericResponse.class)).thenReturn(genericResponseMono);
            when(genericResponseMono.block()).thenReturn(responseEntity);

            String result = spyService.deleteById(id);
            assertEquals("Alice", result);
        }
    }

    @Test
    void testDeleteById_ThrowsApiExceptionOnFailure() {
        String id = "1";
        EmployeeServiceImpl spyService = spy(employeeService);
        EmployeeDTO employee = createEmployee(id, "Alice", 50000);

        ResponseEntity<GenericResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employee).when(spyService).fetchById(id);

            when(webClient.method(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any(Map.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
            when(responseSpec.toEntity(GenericResponse.class)).thenReturn(genericResponseMono);
            when(genericResponseMono.block()).thenReturn(responseEntity);

            ApiException exception = assertThrows(ApiException.class, () -> spyService.deleteById(id));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
            assertTrue(exception.getMessage().contains("Failed to delete employee with id " + id));
        }
    }

    @Test
    void testDeleteById_ThrowsApiExceptionOnFalseResponse() {
        String id = "1";
        EmployeeServiceImpl spyService = spy(employeeService);
        EmployeeDTO employee = createEmployee(id, "Alice", 50000);

        GenericResponse genResponse = new GenericResponse();
        genResponse.setData("false");
        ResponseEntity<GenericResponse> responseEntity = new ResponseEntity<>(genResponse, HttpStatus.OK);

        try (MockedStatic<AopContext> aopContextMock = mockStatic(AopContext.class)) {
            aopContextMock.when(AopContext::currentProxy).thenReturn(spyService);
            doReturn(employee).when(spyService).fetchById(id);

            when(webClient.method(any())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.bodyValue(any(Map.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(utils.addExceptionHandling(responseSpec)).thenReturn(responseSpec);
            when(responseSpec.toEntity(GenericResponse.class)).thenReturn(genericResponseMono);
            when(genericResponseMono.block()).thenReturn(responseEntity);

            ApiException exception = assertThrows(ApiException.class, () -> spyService.deleteById(id));
            assertEquals(HttpStatus.OK, exception.getStatus());
            assertTrue(exception.getMessage().contains("Failed to delete employee with id " + id));
        }
    }

    // Helper method to create test employee DTOs
    private EmployeeDTO createEmployee(String id, String name, int salary) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setSalary(salary);
        dto.setAge(30);
        dto.setTitle("Engineer");
        dto.setEmail(name.toLowerCase() + "@example.com");
        return dto;
    }
}

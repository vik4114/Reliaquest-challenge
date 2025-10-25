package com.reliaquest.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.model.EmployeeListResponse;
import com.reliaquest.api.model.EmployeeResponse;
import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@TestPropertySource(properties = {
        "server.api.url=http://localhost:${mockserver.port}/api/v1"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc mockMvc;
    private static ClientAndServer mockServer;
    private static final String BASE_URL = "/api/v1/employee";

    @BeforeAll
    static void setUp() {
        mockServer = ClientAndServer.startClientAndServer();
        System.setProperty("mockserver.port", String.valueOf(mockServer.getPort()));
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer.reset();
        cacheManager.getCacheNames().forEach(name ->
            Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/v1/employee - Should return all employees")
    void shouldGetAllEmployees() throws Exception {
        List<EmployeeDTO> employees = Arrays.asList(
            createEmployee("1", "John Doe", 75000, 30, "Software Engineer", "john.doe@company.com"),
            createEmployee("2", "Jane Smith", 85000, 28, "Senior Developer", "jane.smith@company.com")
        );
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);
        response.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is("1")))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[0].salary", is(75000)))
            .andExpect(jsonPath("$[1].id", is("2")))
            .andExpect(jsonPath("$[1].name", is("Jane Smith")))
            .andExpect(jsonPath("$[1].salary", is(85000)));
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/v1/employee/{id} - Should return employee by ID")
    void shouldGetEmployeeById() throws Exception {
        String employeeId = "1";
        EmployeeDTO employee = createEmployee(employeeId, "John Doe", 75000, 30, "Software Engineer", "john.doe@company.com");
        EmployeeResponse response = new EmployeeResponse();
        response.setData(employee);
        response.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee/" + employeeId))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response)));

        mockMvc.perform(get(BASE_URL + "/{id}", employeeId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(employeeId)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.salary", is(75000)))
            .andExpect(jsonPath("$.age", is(30)))
            .andExpect(jsonPath("$.title", is("Software Engineer")))
            .andExpect(jsonPath("$.email", is("john.doe@company.com")));
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/v1/employee/{id} - Should return 404 for non-existent employee")
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        String nonExistentId = "999";

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee/" + nonExistentId))
            .respond(response()
                .withStatusCode(404)
                .withHeader("Content-Type", "application/json"));

        mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/employee - Should create new employee")
    void shouldCreateEmployee() throws Exception {
        EmployeeCreateRequest createRequest = new EmployeeCreateRequest();
        createRequest.setName("New Employee");
        createRequest.setSalary(70000);
        createRequest.setAge(27);
        createRequest.setTitle("Software Developer");

        EmployeeDTO createdEmployee = createEmployee("123", "New Employee", 70000, 27, "Software Developer", "new.employee@company.com");
        EmployeeResponse response = new EmployeeResponse();
        response.setData(createdEmployee);
        response.setStatus("success");

        mockServer
            .when(request().withMethod("POST").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response)));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is("123")))
            .andExpect(jsonPath("$.name", is("New Employee")))
            .andExpect(jsonPath("$.salary", is(70000)))
            .andExpect(jsonPath("$.age", is(27)))
            .andExpect(jsonPath("$.title", is("Software Developer")));
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/employee - Should return 400 for invalid employee data")
    void shouldReturn400ForInvalidEmployeeData() throws Exception {
        EmployeeCreateRequest invalidRequest = new EmployeeCreateRequest();
        invalidRequest.setName("");
        invalidRequest.setSalary(-1000);
        invalidRequest.setAge(15);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Should handle rate limiting (429 Too Many Requests)")
    void shouldHandleRateLimiting() throws Exception {
        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(429)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Rate limit exceeded\"}"));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    @Order(7)
    @DisplayName("Should handle server errors (500 Internal Server Error)")
    void shouldHandleServerErrors() throws Exception {
        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"message\":\"Internal server error\"}"));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(8)
    @DisplayName("Should test caching functionality for fetchAll")
    void shouldTestCaching() throws Exception {
        List<EmployeeDTO> employees = Arrays.asList(
            createEmployee("1", "John Doe", 75000, 30, "Software Engineer", "john.doe@company.com")
        );
        EmployeeListResponse response = new EmployeeListResponse();
        response.setData(employees);
        response.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(response)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")));

        mockServer.verify(
            request().withMethod("GET").withPath("/api/v1/employee"),
            VerificationTimes.exactly(1)
        );
    }

    @Test
    @Order(9)
    @DisplayName("Should test cache eviction on create")
    void shouldTestCacheEvictionOnCreate() throws Exception {
        List<EmployeeDTO> initialEmployees = Arrays.asList(
            createEmployee("1", "John Doe", 75000, 30, "Software Engineer", "john.doe@company.com")
        );
        EmployeeListResponse initialResponse = new EmployeeListResponse();
        initialResponse.setData(initialEmployees);
        initialResponse.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(initialResponse)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk());

        EmployeeCreateRequest createRequest = new EmployeeCreateRequest();
        createRequest.setName("New Employee");
        createRequest.setSalary(80000);
        createRequest.setAge(25);
        createRequest.setTitle("Developer");

        EmployeeDTO newEmployee = createEmployee("2", "New Employee", 80000, 25, "Developer", "new@company.com");
        EmployeeResponse createResponse = new EmployeeResponse();
        createResponse.setData(newEmployee);
        createResponse.setStatus("success");

        mockServer
            .when(request().withMethod("POST").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(createResponse)));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());

        List<EmployeeDTO> updatedEmployees = Arrays.asList(
            createEmployee("1", "John Doe", 75000, 30, "Software Engineer", "john.doe@company.com"),
            createEmployee("2", "New Employee", 80000, 25, "Developer", "new@company.com")
        );
        EmployeeListResponse updatedResponse = new EmployeeListResponse();
        updatedResponse.setData(updatedEmployees);
        updatedResponse.setStatus("success");

        mockServer.reset();
        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(updatedResponse)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(10)
    @DisplayName("Should test end-to-end workflow")
    void shouldTestEndToEndWorkflow() throws Exception {
        EmployeeListResponse emptyResponse = new EmployeeListResponse();
        emptyResponse.setData(Arrays.asList());
        emptyResponse.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(emptyResponse)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        EmployeeCreateRequest createRequest1 = new EmployeeCreateRequest();
        createRequest1.setName("Alice Johnson");
        createRequest1.setSalary(90000);
        createRequest1.setAge(28);
        createRequest1.setTitle("Senior Developer");

        EmployeeDTO employee1 = createEmployee("1", "Alice Johnson", 90000, 28, "Senior Developer", "alice@company.com");
        EmployeeResponse createResponse1 = new EmployeeResponse();
        createResponse1.setData(employee1);
        createResponse1.setStatus("success");

        mockServer
            .when(request().withMethod("POST").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(createResponse1)));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Alice Johnson")));

        EmployeeCreateRequest createRequest2 = new EmployeeCreateRequest();
        createRequest2.setName("Bob Smith");
        createRequest2.setSalary(75000);
        createRequest2.setAge(25);
        createRequest2.setTitle("Developer");

        EmployeeDTO employee2 = createEmployee("2", "Bob Smith", 75000, 25, "Developer", "bob@company.com");
        EmployeeResponse createResponse2 = new EmployeeResponse();
        createResponse2.setData(employee2);
        createResponse2.setStatus("success");

        mockServer.reset();
        mockServer
            .when(request().withMethod("POST").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(201)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(createResponse2)));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest2)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Bob Smith")));

        List<EmployeeDTO> allEmployees = Arrays.asList(employee1, employee2);
        EmployeeListResponse allResponse = new EmployeeListResponse();
        allResponse.setData(allEmployees);
        allResponse.setStatus("success");

        mockServer
            .when(request().withMethod("GET").withPath("/api/v1/employee"))
            .respond(response()
                .withStatusCode(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(allResponse)));

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
            .andExpect(jsonPath("$[1].name", is("Bob Smith")));
    }

    private EmployeeDTO createEmployee(String id, String name, Integer salary, Integer age, String title, String email) {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(id);
        employee.setName(name);
        employee.setSalary(salary);
        employee.setAge(age);
        employee.setTitle(title);
        employee.setEmail(email);
        return employee;
    }
}

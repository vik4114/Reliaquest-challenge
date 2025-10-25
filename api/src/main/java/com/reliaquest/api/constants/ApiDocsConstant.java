package com.reliaquest.api.constants;

public class ApiDocsConstant {

    private ApiDocsConstant() {
        // Prevent instantiation
    }

    public static final String GET_ALL_EMPLOYEES_SUMMARY = "Retrieve all employees";
    public static final String GET_ALL_EMPLOYEES_DESCRIPTION = "Returns the complete list of employees stored in the system";

    public static final String SEARCH_EMPLOYEES_BY_NAME_SUMMARY = "Find employees by name";
    public static final String SEARCH_EMPLOYEES_BY_NAME_DESCRIPTION =
            "Returns employees whose names partially or fully match the input value";

    public static final String GET_EMPLOYEE_BY_ID_SUMMARY = "Fetch employee details by ID";
    public static final String GET_EMPLOYEE_BY_ID_DESCRIPTION =
            "Retrieves detailed information about a specific employee using their unique ID";

    public static final String GET_HIGHEST_SALARY_SUMMARY = "Retrieve the highest employee salary";
    public static final String GET_HIGHEST_SALARY_DESCRIPTION =
            "Fetches the maximum salary recorded among all employees";

    public static final String GET_TOP_10_HIGHEST_EARNING_EMPLOYEES_SUMMARY = "Retrieve top 10 highest-paid employees";
    public static final String GET_TOP_10_HIGHEST_EARNING_EMPLOYEES_DESCRIPTION =
            "Provides a list of the 10 employees earning the most, ordered by salary from high to low";

    public static final String CREATE_EMPLOYEE_SUMMARY = "Add a new employee";
    public static final String CREATE_EMPLOYEE_DESCRIPTION =
            "Creates and saves a new employee record into the system database";

    public static final String DELETE_AN_EMPLOYEE_BY_ID_SUMMARY = "Remove an employee by ID";
    public static final String DELETES_AN_EMPLOYEE_BY_ID_DESCRIPTION =
            "Deletes the employee entry associated with the specified ID from the database";
}


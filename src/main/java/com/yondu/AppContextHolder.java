package com.yondu;

/** This will serve as a single instance application context holder that
 *  contains shared data within the application.
 *
 *  @employeeId - logged in employee
 *  @authorizationToken - we don't want to request for token each api call so we store it here
 */
public class AppContextHolder {

    private String authorizationToken;
    private String employeeId;
    private String employeeName;
    private String customerId;
    private String customerAppAuthToken;

    public String getCustomerAppAuthToken() {
        return customerAppAuthToken;
    }

    public void setCustomerAppAuthToken(String customerAppAuthToken) {
        this.customerAppAuthToken = customerAppAuthToken;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}

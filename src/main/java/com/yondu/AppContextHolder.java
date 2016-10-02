package com.yondu;

import javafx.stage.Stage;

/** This will serve as a single instance application context holder that
 *  contains shared data within the application.
 *
 *  @employeeId - logged in employee
 *  @authorizationToken - we don't want to request for token each api call so we store it here
 */
public class AppContextHolder {

    private String authorizationToken;
    private String customerAppAuthToken;

    //logged in employee data
    private String employeeId;
    private String employeeName;

    //logged in customer data
    private String customerId;
    private String customerName;
    private String customerMobile;

    //Temporary OCR configuration
    private Integer salesPosX;
    private Integer salesPosY;
    private Integer salesWidth;
    private Integer salesHeight;

    private Integer orNumberPosX;
    private Integer orNumberPosY;
    private Integer orNumberWidth;
    private Integer orNumberHeight;

    //Stage references
    private Stage homeStage;

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public Stage getHomeStage() {
        return homeStage;
    }

    public void setHomeStage(Stage homeStage) {
        this.homeStage = homeStage;
    }

    public Integer getOrNumberPosX() {
        return orNumberPosX;
    }

    public void setOrNumberPosX(Integer orNumberPosX) {
        this.orNumberPosX = orNumberPosX;
    }

    public Integer getOrNumberPosY() {
        return orNumberPosY;
    }

    public void setOrNumberPosY(Integer orNumberPosY) {
        this.orNumberPosY = orNumberPosY;
    }

    public Integer getOrNumberWidth() {
        return orNumberWidth;
    }

    public void setOrNumberWidth(Integer orNumberWidth) {
        this.orNumberWidth = orNumberWidth;
    }

    public Integer getOrNumberHeight() {
        return orNumberHeight;
    }

    public void setOrNumberHeight(Integer orNumberHeight) {
        this.orNumberHeight = orNumberHeight;
    }

    public Integer getSalesPosX() {
        return salesPosX;
    }

    public void setSalesPosX(Integer salesPosX) {
        this.salesPosX = salesPosX;
    }

    public Integer getSalesPosY() {
        return salesPosY;
    }

    public void setSalesPosY(Integer salesPosY) {
        this.salesPosY = salesPosY;
    }

    public Integer getSalesWidth() {
        return salesWidth;
    }

    public void setSalesWidth(Integer salesWidth) {
        this.salesWidth = salesWidth;
    }

    public Integer getSalesHeight() {
        return salesHeight;
    }

    public void setSalesHeight(Integer salesHeight) {
        this.salesHeight = salesHeight;
    }

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}

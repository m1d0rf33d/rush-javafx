package com.yondu.model;

import java.util.List;

/**
 * Created by lynx on 2/21/17.
 */
public class Employee {

    private String employeeId;
    private String employeeName;
    private String branchId;
    private List<String> screenAccess;

    public List<String> getScreenAccess() {
        return screenAccess;
    }

    public void setScreenAccess(List<String> screenAccess) {
        this.screenAccess = screenAccess;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}

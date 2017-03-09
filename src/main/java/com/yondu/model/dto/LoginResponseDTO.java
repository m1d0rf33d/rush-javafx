package com.yondu.model.dto;

import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
public class LoginResponseDTO {

    private EmployeeDTO employeeDTO;
    private MerchantDTO merchantDTO;
    private List<String> screenAccess;

    public EmployeeDTO getEmployeeDTO() {
        return employeeDTO;
    }

    public void setEmployeeDTO(EmployeeDTO employeeDTO) {
        this.employeeDTO = employeeDTO;
    }

    public MerchantDTO getMerchantDTO() {
        return merchantDTO;
    }

    public void setMerchantDTO(MerchantDTO merchantDTO) {
        this.merchantDTO = merchantDTO;
    }

    public List<String> getScreenAccess() {
        return screenAccess;
    }

    public void setScreenAccess(List<String> screenAccess) {
        this.screenAccess = screenAccess;
    }
}

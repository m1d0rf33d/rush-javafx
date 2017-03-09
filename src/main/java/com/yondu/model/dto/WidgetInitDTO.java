package com.yondu.model.dto;

import java.util.List;

/**
 * Created by aomine on 3/9/17.
 */
public class WidgetInitDTO {

    private MerchantDTO merchantDTO;
    private List<BranchDTO> branchDTOs;


    public MerchantDTO getMerchantDTO() {
        return merchantDTO;
    }

    public void setMerchantDTO(MerchantDTO merchantDTO) {
        this.merchantDTO = merchantDTO;
    }

    public List<BranchDTO> getBranchDTOs() {
        return branchDTOs;
    }

    public void setBranchDTOs(List<BranchDTO> branchDTOs) {
        this.branchDTOs = branchDTOs;
    }
}

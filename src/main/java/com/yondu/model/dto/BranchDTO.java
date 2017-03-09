package com.yondu.model.dto;

/**
 * Created by aomine on 11/2/16.
 */
public class BranchDTO {
    private String branchName;
    private Long branchId;
    private Boolean withVk;
    private String uuid;

    private String logoUrl;

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Boolean getWithVk() {
        return withVk;
    }

    public void setWithVk(Boolean withVk) {
        this.withVk = withVk;
    }
}

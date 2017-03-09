package com.yondu.model.dto;

/**
 * Created by aomine on 10/18/16.
 */
public class MerchantDTO {

    private Long id;

    private String name;
    private String uniqueKey;
    private String merchantApiKey;
    private String merchantApiSecret;

    private String customerApiKey;
    private String customerApiSecret;
    private String status;

    private String token;
    private Boolean withVk;

    private String backgroundUrl;
    private String stampsUrl;
    private String grayStampsUrl;
    private String merchantType;

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getStampsUrl() {
        return stampsUrl;
    }

    public void setStampsUrl(String stampsUrl) {
        this.stampsUrl = stampsUrl;
    }

    public String getGrayStampsUrl() {
        return grayStampsUrl;
    }

    public void setGrayStampsUrl(String grayStampsUrl) {
        this.grayStampsUrl = grayStampsUrl;
    }

    public Boolean getWithVk() {
        return withVk;
    }

    public void setWithVk(Boolean withVk) {
        this.withVk = withVk;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getMerchantApiKey() {
        return merchantApiKey;
    }

    public void setMerchantApiKey(String merchantApiKey) {
        this.merchantApiKey = merchantApiKey;
    }

    public String getMerchantApiSecret() {
        return merchantApiSecret;
    }

    public void setMerchantApiSecret(String merchantApiSecret) {
        this.merchantApiSecret = merchantApiSecret;
    }

    public String getCustomerApiKey() {
        return customerApiKey;
    }

    public void setCustomerApiKey(String customerApiKey) {
        this.customerApiKey = customerApiKey;
    }

    public String getCustomerApiSecret() {
        return customerApiSecret;
    }

    public void setCustomerApiSecret(String customerApiSecret) {
        this.customerApiSecret = customerApiSecret;
    }
}

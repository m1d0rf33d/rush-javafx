package com.yondu;

import com.yondu.service.ApiService;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** This will serve as a single instance application context holder that
 *  contains shared data within the application. This is the heart of the application.
 *
 *  @employeeId - logged in employee
 *  @authorizationToken - we don't want to request for token each api call so we store it here
 */
public class AppContextHolder {

    private String authorizationToken;
    private String customerAppAuthToken;

    //Reference for logged in employee
    private String employeeId;
    private String employeeName;
    private String branchId;

    //Reference for logged in customer
    private String customerMobile;
    private String customerUUID;

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
    private boolean onlineMode;
    private Stage orCaptureStage;
    private Stage salesCaptureStage;
    private Stage loadingStage;

    //static path reference
    private String ocrFullPath;
    private String offlinePath;

    //Read endpoints from config file
    private String baseUrl;
    private String registerEndpoint;
    private String memberLoginEndpoint;
    private String pointsConversionEndpoint;
    private String givePointsEndpoint;
    private String getPointsEndpoint;
    private String payWithPointsEndpoint;
    private String getRewardsEndpoint;
    private String redeemRewardsEndpoint;
    private String unclaimedRewardsEndpoint;
    private String claimRewardsEndpoint;
    private String getRewardsMerchantEndpoint;
    private String customerRewardsEndpoint;
    private String customerTransactionsEndpoint;
    private String getBranchesEndpoint;
    private String loginEndpoint;
    private String merchantDesignsEndpoint;
    private String merchantSettingsEndpoint;

    private String appKey;
    private String appSecret;
    private String customerAppKey;
    private String customerAppSecret;
    private String authorizationEndpoint;
    private String guestEarnEndpoint;

    private boolean isActivated;
    private String previousStage;
    private Boolean withVk;
    private Boolean is64Bit;
    private Boolean isFirstDisconnect;

    public Boolean getFirstDisconnect() {
        return isFirstDisconnect;
    }

    public void setFirstDisconnect(Boolean firstDisconnect) {
        isFirstDisconnect = firstDisconnect;
    }

    public String getGuestEarnEndpoint() {
        return guestEarnEndpoint;
    }

    public void setGuestEarnEndpoint(String guestEarnEndpoint) {
        this.guestEarnEndpoint = guestEarnEndpoint;
    }

    public Boolean getIs64Bit() {
        return is64Bit;
    }

    public void setIs64Bit(Boolean is64Bit) {
        this.is64Bit = is64Bit;
    }

    public Boolean getWithVk() {
        return withVk;
    }

    public void setWithVk(Boolean withVk) {
        this.withVk = withVk;
    }

    public String getMerchantSettingsEndpoint() {
        return merchantSettingsEndpoint;
    }

    public void setMerchantSettingsEndpoint(String merchantSettingsEndpoint) {
        this.merchantSettingsEndpoint = merchantSettingsEndpoint;
    }

    public String getMerchantDesignsEndpoint() {
        return merchantDesignsEndpoint;
    }

    public void setMerchantDesignsEndpoint(String merchantDesignsEndpoint) {
        this.merchantDesignsEndpoint = merchantDesignsEndpoint;
    }

    public String getPreviousStage() {
        return previousStage;
    }

    public void setPreviousStage(String previousStage) {
        this.previousStage = previousStage;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public Stage getLoadingStage() {
        return loadingStage;
    }

    public void setLoadingStage(Stage loadingStage) {
        this.loadingStage = loadingStage;
    }

    private ApiService apiService;

    public ApiService getApiService() {
        return apiService;
    }

    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getCustomerAppKey() {
        return customerAppKey;
    }

    public void setCustomerAppKey(String customerAppKey) {
        this.customerAppKey = customerAppKey;
    }

    public String getCustomerAppSecret() {
        return customerAppSecret;
    }

    public void setCustomerAppSecret(String customerAppSecret) {
        this.customerAppSecret = customerAppSecret;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public void setLoginEndpoint(String loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public String getGetBranchesEndpoint() {
        return getBranchesEndpoint;
    }

    public void setGetBranchesEndpoint(String getBranchesEndpoint) {
        this.getBranchesEndpoint = getBranchesEndpoint;
    }

    public String getRegisterEndpoint() {
        return registerEndpoint;
    }

    public void setRegisterEndpoint(String registerEndpoint) {
        this.registerEndpoint = registerEndpoint;
    }

    public String getMemberLoginEndpoint() {
        return memberLoginEndpoint;
    }

    public void setMemberLoginEndpoint(String memberLoginEndpoint) {
        this.memberLoginEndpoint = memberLoginEndpoint;
    }

    public String getPointsConversionEndpoint() {
        return pointsConversionEndpoint;
    }

    public void setPointsConversionEndpoint(String pointsConversionEndpoint) {
        this.pointsConversionEndpoint = pointsConversionEndpoint;
    }

    public String getGivePointsEndpoint() {
        return givePointsEndpoint;
    }

    public void setGivePointsEndpoint(String givePointsEndpoint) {
        this.givePointsEndpoint = givePointsEndpoint;
    }

    public String getGetPointsEndpoint() {
        return getPointsEndpoint;
    }

    public void setGetPointsEndpoint(String getPointsEndpoint) {
        this.getPointsEndpoint = getPointsEndpoint;
    }

    public String getPayWithPointsEndpoint() {
        return payWithPointsEndpoint;
    }

    public void setPayWithPointsEndpoint(String payWithPointsEndpoint) {
        this.payWithPointsEndpoint = payWithPointsEndpoint;
    }

    public String getGetRewardsEndpoint() {
        return getRewardsEndpoint;
    }

    public void setGetRewardsEndpoint(String getRewardsEndpoint) {
        this.getRewardsEndpoint = getRewardsEndpoint;
    }

    public String getRedeemRewardsEndpoint() {
        return redeemRewardsEndpoint;
    }

    public void setRedeemRewardsEndpoint(String redeemRewardsEndpoint) {
        this.redeemRewardsEndpoint = redeemRewardsEndpoint;
    }

    public String getUnclaimedRewardsEndpoint() {
        return unclaimedRewardsEndpoint;
    }

    public void setUnclaimedRewardsEndpoint(String unclaimedRewardsEndpoint) {
        this.unclaimedRewardsEndpoint = unclaimedRewardsEndpoint;
    }

    public String getClaimRewardsEndpoint() {
        return claimRewardsEndpoint;
    }

    public void setClaimRewardsEndpoint(String claimRewardsEndpoint) {
        this.claimRewardsEndpoint = claimRewardsEndpoint;
    }

    public String getGetRewardsMerchantEndpoint() {
        return getRewardsMerchantEndpoint;
    }

    public void setGetRewardsMerchantEndpoint(String getRewardsMerchantEndpoint) {
        this.getRewardsMerchantEndpoint = getRewardsMerchantEndpoint;
    }

    public String getCustomerRewardsEndpoint() {
        return customerRewardsEndpoint;
    }

    public void setCustomerRewardsEndpoint(String customerRewardsEndpoint) {
        this.customerRewardsEndpoint = customerRewardsEndpoint;
    }

    public String getCustomerTransactionsEndpoint() {
        return customerTransactionsEndpoint;
    }

    public void setCustomerTransactionsEndpoint(String customerTransactionsEndpoint) {
        this.customerTransactionsEndpoint = customerTransactionsEndpoint;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Stage getSalesCaptureStage() {
        return salesCaptureStage;
    }

    public void setSalesCaptureStage(Stage salesCaptureStage) {
        this.salesCaptureStage = salesCaptureStage;
    }

    public Stage getOrCaptureStage() {
        return orCaptureStage;
    }

    public void setOrCaptureStage(Stage orCaptureStage) {
        this.orCaptureStage = orCaptureStage;
    }

    public String getOfflinePath() {
        return offlinePath;
    }

    public void setOfflinePath(String offlinePath) {
        this.offlinePath = offlinePath;
    }

    public String getOcrFullPath() {
        return ocrFullPath;
    }

    public void setOcrFullPath(String ocrFullPath) {
        this.ocrFullPath = ocrFullPath;
    }

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
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

    public String getCustomerAppAuthToken() {
        return customerAppAuthToken;
    }

    public void setCustomerAppAuthToken(String customerAppAuthToken) {
        this.customerAppAuthToken = customerAppAuthToken;
    }

    public String getEmployeeId() {
        if (this.employeeId == null) {
            return "OFFLINE_EMPLOYEE";
        }
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
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

    public Stage getHomeStage() {
        return homeStage;
    }

    public void setHomeStage(Stage homeStage) {
        this.homeStage = homeStage;
    }

    public String getCustomerUUID() {
        return customerUUID;
    }

    public void setCustomerUUID(String customerUUID) {
        this.customerUUID = customerUUID;
    }
}

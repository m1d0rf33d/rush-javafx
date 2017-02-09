package com.yondu;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** This will serve as a single instance application context holder that
 *  contains shared data within the application. This is the heart of the application.
 *
 *  @employeeId - logged in employee
 *  @authorizationToken - we don't want to request for token each api call so we store it here
 */
public class AppContextHolder {

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
    private boolean isActivated;
    private String previousStage;
    private Boolean withVk;
    private Boolean isFirstDisconnect;

    private VBox rootVBox;
    private StackPane rootStackPane;

    //Read endpoints from config file
    public static String BASE_URL;
    public static String CMS_URL;
    public static String TOMCAT_PORT;

    public static String LOGIN_ENDPOINT;
    public static String AUTHORIZATION_ENDPOINT;
    public static String REGISTER_ENDPOINT;
    public static String MEMBER_LOGIN_ENDPOINT;
    public static String POINTS_CONVERSION_ENDPOINT;
    public static String GIVE_POINTS_ENDPOINT;
    public static String GET_POINTS_ENDPOINT;
    public static String PAY_WITH_POINTS_ENDPOINT;
    public static String GET_REWARDS_ENDPOINT;
    public static String GET_REWARDS_MERCHANT_ENDPOINT;
    public static String REDEEM_REWARDS_ENDPOINT;
    public static String UNCLAIMED_REWARDS_ENDPOINT;
    public static String CLAIM_REWARDS_ENDPOINT;
    public static String GET_BRANCHES_ENDPOINT;
    public static String CUSTOMER_REWARDS_ENDPOINT;
    public static String CUSTOMER_TRANSACTION_ENDPOINT;
    public static String MERCHANT_DESIGNS_ENDPOINT;
    public static String MERCHANT_SETTINGS_ENDPOINT;
    public static String EARN_GUEST_ENDPOINT;
    public static String OAUTH_ENDPOINT;
    public static String OAUTH_SECRET;
    public static String VALIDATE_MERCHANT_ENDPOINT;
    public static String ACCESS_ENDPOINT;

    public static String MERCHANT_APP_KEY;
    public static String MERCHANT_APP_SECRET;
    public static String CUSTOMER_APP_KEY;
    public static String CUSTOMER_APP_SECRET;

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    public void setRootStackPane(StackPane rootStackPane) {
        this.rootStackPane = rootStackPane;
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

    public VBox getRootVBox() {
        return rootVBox;
    }

    public void setRootVBox(VBox rootVBox) {
        this.rootVBox = rootVBox;
    }

    public String getCustomerMobile() {
        return customerMobile;
    }

    public void setCustomerMobile(String customerMobile) {
        this.customerMobile = customerMobile;
    }

    public String getCustomerUUID() {
        return customerUUID;
    }

    public void setCustomerUUID(String customerUUID) {
        this.customerUUID = customerUUID;
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

    public boolean isOnlineMode() {
        return onlineMode;
    }

    public void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
    }

    public Stage getOrCaptureStage() {
        return orCaptureStage;
    }

    public void setOrCaptureStage(Stage orCaptureStage) {
        this.orCaptureStage = orCaptureStage;
    }

    public Stage getSalesCaptureStage() {
        return salesCaptureStage;
    }

    public void setSalesCaptureStage(Stage salesCaptureStage) {
        this.salesCaptureStage = salesCaptureStage;
    }

    public Stage getLoadingStage() {
        return loadingStage;
    }

    public void setLoadingStage(Stage loadingStage) {
        this.loadingStage = loadingStage;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public String getPreviousStage() {
        return previousStage;
    }

    public void setPreviousStage(String previousStage) {
        this.previousStage = previousStage;
    }

    public Boolean getWithVk() {
        return withVk;
    }

    public void setWithVk(Boolean withVk) {
        this.withVk = withVk;
    }

    public Boolean getFirstDisconnect() {
        return isFirstDisconnect;
    }

    public void setFirstDisconnect(Boolean firstDisconnect) {
        isFirstDisconnect = firstDisconnect;
    }


    /*public String getEmployeeId() {
        if (this.employeeId == null) {
            return "OFFLINE_EMPLOYEE";
        }
        return employeeId;
    }*/

}

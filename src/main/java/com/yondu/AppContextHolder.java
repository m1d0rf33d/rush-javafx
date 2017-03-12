package com.yondu;

import com.yondu.model.*;
import com.yondu.model.constants.AppState;
import com.yondu.model.dto.MerchantDTO;
import com.yondu.service.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/** This will serve as a single instance application context holder that
 *  contains shared data within the application. I don't want to fckng pass shared variables on every
 *  component of the application so we create this GLOBAL space that every component can access.
 *
 */
public class AppContextHolder {

    private AppState currentState;
    private AppState prevState;
    private Merchant merchant;
    private Reward reward;
    private Customer customer;
    private Branch branch;
    private Employee employee;
    private VBox rootContainer;
    private PointsRule pointsRule;

    private List<Branch> branches;
    private Stage orCaptureStage;
    private Stage salesCaptureStage;
    private OcrConfig ocrConfig;
    private List<Reward> unclaimedRewards = new ArrayList<>();

    private MerchantDTO merchantDTO;

    public static ApiService apiService = new ApiService();
    public static CommonService commonService = new CommonService();
    public static EarnPointsService earnPointsService = new EarnPointsService();
    public static GuestPurchaseService guestPurchaseService = new GuestPurchaseService();
    public static IssueRewardsService issueRewardsService = new IssueRewardsService();
    public static LoginService loginService = new LoginService();
    public static MemberDetailsService memberDetailsService = new MemberDetailsService();
    public static MenuService menuService = new MenuService();
    public static OcrService ocrService = new OcrService();
    public static OfflineService offlineService = new OfflineService();
    public static PayWithPointsService payWithPointsService = new PayWithPointsService();
    public static RedeemRewardsService redeemRewardsService = new RedeemRewardsService();
    public static RegisterService registerService = new RegisterService();
    public static RouteService routeService = new RouteService();
    public static StampsService stampsService = new StampsService();
    public static BranchTransactionService branchTransactionService = new BranchTransactionService();

    public PointsRule getPointsRule() {
        return pointsRule;
    }

    public void setPointsRule(PointsRule pointsRule) {
        this.pointsRule = pointsRule;
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

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public VBox getRootContainer() {
        return rootContainer;
    }

    public void setRootContainer(VBox rootContainer) {
        this.rootContainer = rootContainer;
    }

    public AppState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(AppState currentState) {
        this.currentState = currentState;
    }

    public AppState getPrevState() {
        return prevState;
    }

    public void setPrevState(AppState prevState) {
        this.prevState = prevState;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public MerchantDTO getMerchantDTO() {
        return merchantDTO;
    }

    public void setMerchantDTO(MerchantDTO merchantDTO) {
        this.merchantDTO = merchantDTO;
    }
}

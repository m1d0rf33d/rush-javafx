package com.yondu.model;

import java.util.List;

/**
 * Created by erwin on 3/1/2017.
 */
public class Merchant {

    private String backgroundUrl;
    private String stampsUrl;
    private String grayStampsUrl;
    private List<Reward> rewards;

    private String uniqueKey;
    private String token;
    private Boolean withVk;
    private String merchantType;

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getUniqueKey() {
        return uniqueKey;
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

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getWithVk() {
        return withVk;
    }

    public void setWithVk(Boolean withVk) {
        this.withVk = withVk;
    }
}

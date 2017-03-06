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

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
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
}

package com.yondu.model;

import java.util.List;

/**
 * Created by erwin on 2/28/2017.
 */
public class Promo {

    private List<Reward> rewards;
    private Long stamps;

    public Long getStamps() {
        return stamps;
    }

    public void setStamps(Long stamps) {
        this.stamps = stamps;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }
}

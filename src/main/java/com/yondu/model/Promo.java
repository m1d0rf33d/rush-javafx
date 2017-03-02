package com.yondu.model;

import java.util.List;

/**
 * Created by erwin on 2/28/2017.
 */
public class Promo {

    private List<Reward> rewards;
    private int stamps;

    public int getStamps() {
        return stamps;
    }

    public void setStamps(int stamps) {
        this.stamps = stamps;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }
}

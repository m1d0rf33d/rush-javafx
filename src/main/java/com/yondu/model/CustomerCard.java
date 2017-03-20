package com.yondu.model;

import java.util.List;

/**
 * Created by erwin on 2/28/2017.
 */
public class CustomerCard {

    Promo promo;
    Long stampCount;
    List<Reward> rewards;

    public List<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public Long getStampCount() {
        return stampCount;
    }

    public void setStampCount(Long stampCount) {
        this.stampCount = stampCount;
    }

    public Promo getPromo() {
        return promo;
    }

    public void setPromo(Promo promo) {
        this.promo = promo;
    }
}

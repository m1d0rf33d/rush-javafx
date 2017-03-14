package com.yondu.model;

/**
 * Created by aomine on 3/12/17.
 */
public enum TransactionType {

    REGISTER("Register"), EARN_POINTS("Earn points"), REDEEM_REWARDS("Redeem rewards"),
    EARN_OFFLINE("Earn points offline"), PAY_WITH_POINTS("Pay with points"), EARN_POINTS_GUEST("Earn points guest"),
    ISSUE_REWARD("Issue reward");

    private String value;

    private TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


}

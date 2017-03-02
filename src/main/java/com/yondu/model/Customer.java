package com.yondu.model;

import java.util.List;

/**
 * Created by lynx on 2/9/17.
 */
public class Customer {

    private String memberId;
    private String name;
    private String mobileNumber;
    private String email;
    private String dateOfBirth;
    private String gender;
    private String memberSince;
    private String availablePoints;
    private List<Reward> activeVouchers;
    private CustomerCard card;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CustomerCard getCard() {
        return card;
    }

    public void setCard(CustomerCard card) {
        this.card = card;
    }

    public List<Reward> getActiveVouchers() {
        return activeVouchers;
    }

    public void setActiveVouchers(List<Reward> activeVouchers) {
        this.activeVouchers = activeVouchers;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public String getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(String availablePoints) {
        this.availablePoints = availablePoints;
    }
}

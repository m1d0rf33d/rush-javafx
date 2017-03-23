package com.yondu.model;

/**
 * Created by lynx on 2/9/17.
 */
public class Reward {

    private String imageUrl;
    private String name;
    private String details;
    private String id;
    private String pointsRequired;
    private String quantity;
    private String date;
    private Integer stamps;
    private String redeemId;
    private Boolean status;
    private String quantityToIssue;

    public String getQuantityToIssue() {
        return quantityToIssue;
    }

    public void setQuantityToIssue(String quantityToIssue) {
        this.quantityToIssue = quantityToIssue;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getRedeemId() {
        return redeemId;
    }

    public void setRedeemId(String redeemId) {
        this.redeemId = redeemId;
    }

    public Integer getStamps() {
        return stamps;
    }

    public void setStamps(Integer stamps) {
        this.stamps = stamps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(String pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

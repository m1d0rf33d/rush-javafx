package com.yondu.model;

/**
 * Created by lynx on 2/7/17.
 */
public class Transaction {

    private String transactionType;
    private String receiptNumber;
    private String date;
    private String pointsPaid;
    private String cashPaid;
    private String pointsEarned;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPointsPaid() {
        return pointsPaid;
    }

    public void setPointsPaid(String pointsPaid) {
        this.pointsPaid = pointsPaid;
    }

    public String getCashPaid() {
        return cashPaid;
    }

    public void setCashPaid(String cashPaid) {
        this.cashPaid = cashPaid;
    }

    public String getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(String pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getTransactionType() {

        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}

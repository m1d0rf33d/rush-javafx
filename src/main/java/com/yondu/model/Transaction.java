package com.yondu.model;

/**
 * Created by lynx on 2/7/17.
 */
public class Transaction {

    private String id;
    private String type;

    public Transaction(String id, String type) {
        this.id =  id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

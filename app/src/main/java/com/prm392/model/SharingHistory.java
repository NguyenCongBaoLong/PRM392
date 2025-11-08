package com.prm392.model;

import java.util.Date;

public class SharingHistory {
    private String id;
    private String recipient;
    private Date date;
    private String status;  // "Active" or "Expired"
    private String userUid;

    public SharingHistory() {}

    public SharingHistory(String recipient, Date date, String status, String userUid) {
        this.recipient = recipient;
        this.date = date;
        this.status = status;
        this.userUid = userUid;
    }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRecipient() { return recipient; }
    public String getStatus() { return status; }
    public Date getDate() { return date; }
    public String getUserUid() { return userUid; }
}
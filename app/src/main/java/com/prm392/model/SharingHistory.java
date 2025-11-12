package com.prm392.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class SharingHistory implements Serializable {
    private String id; // Document ID from Firestore
    private String userId;
    private String certificateId;
    private String shareType; // e.g., "link", "email", "text"
    private Timestamp timestamp; // Use Timestamp for Firestore compatibility
    private String status; // e.g., "success", "failed"

    // Default constructor for Firestore
    public SharingHistory() {}

    public SharingHistory(String userId, String certificateId, String shareType, Timestamp timestamp, String status) {
        this.userId = userId;
        this.certificateId = certificateId;
        this.shareType = shareType;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCertificateId() { return certificateId; }
    public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
    public String getShareType() { return shareType; }
    public void setShareType(String shareType) { this.shareType = shareType; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
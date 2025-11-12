package com.prm392.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Certificate implements Serializable {
    private String id;
    private String certificateName;
    private String issuingOrganization;
    private String credentialId;
    private Date issueDate;
    private Date expirationDate;
    private String fileUrl;
    private String fileName;
    private String userId;
    private List<Tag> tags;
    private boolean isArchived;

    public Certificate() {
        this.isArchived = false;  // Default không archived
    }

    public Certificate(String certificateId, String certificateName, String issuingOrganization,
                       String credentialId, Date issueDate, Date expirationDate,
                       String fileUrl, String fileName, String userId) {
        this.id = certificateId;
        this.certificateName = certificateName;
        this.issuingOrganization = issuingOrganization;
        this.credentialId = credentialId;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.userId = userId;
        this.isArchived = false;  // Default
    }
    public Certificate( String certificateName, String issuingOrganization,
                        String credentialId, Date issueDate, Date expirationDate,
                        String fileUrl, String fileName, String userId) {

        this.certificateName = certificateName;
        this.issuingOrganization = issuingOrganization;
        this.credentialId = credentialId;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.userId = userId;
        this.isArchived = false;  // Default
    }

    // GETTERS & SETTERS
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public String getCertificateName() { return certificateName; }
    public void setCertificateName(String certificateName) { this.certificateName = certificateName; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
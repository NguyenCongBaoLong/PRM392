package com.prm392.model;

import java.util.Date;

public class Certificate {
    private String id;
    private String certificateName;
    private String issuingOrganization;
    private String credentialId;
    private Date issueDate;
    private Date expirationDate;
    private String fileUrl;
    private String fileName;
    private String userId;

    public Certificate() {}

    public Certificate(String certificateName, String issuingOrganization,
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
    }

    // GETTERS & SETTERS
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
}
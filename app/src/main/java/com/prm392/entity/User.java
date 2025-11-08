package com.prm392.entity;

public class User {

    private String uid;
    private String fullName;
    private String email;
    private String password;

    private String imageUrl;
    public User() {
    }

    public User(String uid, String fullName, String email){
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;

    }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getImageUrl(){return imageUrl;}

    public void setImageUrl(String imageUrl){this.imageUrl = imageUrl;}

}

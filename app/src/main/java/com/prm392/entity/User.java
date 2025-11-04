package com.prm392.entity;

public class User {

    private String uid;
    private String fullName;
    private String email;
    private String password;

    public User() {
    }

    public User(String uid, String fullName, String email, String password){
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }
    public String getUid(){return uid;}
    public String getFullName(){return fullName;}
    public String getEmail(){return email;}
    public String getPassword(){return password;}

}

package com.prm392.onlineshoesshop.model;

public class User {
    public String uid;
    public String email;
    public String fullName;
    public String profileImageUrl;
    public Address address;

    public User(String uid, String email, String fullName, String profileImageUrl, Address address) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

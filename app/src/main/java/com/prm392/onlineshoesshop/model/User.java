package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    public String uid;
    public String email;
    public String fullName;
    public String profileImageUrl;
    public Address address;
    public boolean googleAccount;

    public User() {
    }

    public User(String uid, String email, String fullName, String profileImageUrl, Address address, boolean googleAccount) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.googleAccount = googleAccount;
    }

    protected User(Parcel in) {
        uid = in.readString();
        email = in.readString();
        fullName = in.readString();
        profileImageUrl = in.readString();
        googleAccount = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    public boolean isGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(boolean googleAccount) {
        this.googleAccount = googleAccount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(fullName);
        dest.writeString(profileImageUrl);
        dest.writeByte((byte) (googleAccount ? 1 : 0));
    }
}

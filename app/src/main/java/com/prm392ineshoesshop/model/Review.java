package com.prm392ineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Review implements Parcelable {
    private String reviewId;
    private String itemId;
    private String userId;
    private String userName;
    private String userAvatar;
    private double rating;
    private String comment;
    private long createdAt;
    private String userImageUrl;

    public Review() {
    }

    public Review(String reviewId, String itemId, String userId, String userName,
            String userAvatar, double rating, String comment, long createdAt) {
        this.reviewId = reviewId;
        this.itemId = itemId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    // Parcelable implementation
    protected Review(Parcel in) {
        reviewId = in.readString();
        itemId = in.readString();
        userId = in.readString();
        userName = in.readString();
        userAvatar = in.readString();
        rating = in.readDouble();
        comment = in.readString();
        createdAt = in.readLong();
        userImageUrl = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(reviewId);
        dest.writeString(itemId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(userAvatar);
        dest.writeDouble(rating);
        dest.writeString(comment);
        dest.writeLong(createdAt);
        dest.writeString(userImageUrl);
    }
}
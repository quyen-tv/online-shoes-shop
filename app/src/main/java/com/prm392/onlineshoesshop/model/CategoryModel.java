package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class CategoryModel implements Parcelable {


    private String Id;
    private String title;
    private String picUrl;

    public CategoryModel() {
    }

    public CategoryModel(String categoryId, String title, String picUrl) {
        this.Id = categoryId;
        this.title = title;
        this.picUrl = picUrl;
    }

    public String getId() {
        return Id;
    }

    public void setId(long id) {
        this.Id = String.valueOf(id);
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected CategoryModel(Parcel in) {
        Id = in.readString();
        title = in.readString();
        picUrl = in.readString();
    }

    public static final Creator<CategoryModel> CREATOR = new Creator<>() {
        @Override
        public CategoryModel createFromParcel(Parcel in) {
            return new CategoryModel(in);
        }

        @Override
        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(Id);
        dest.writeString(title);
        dest.writeString(picUrl);
    }
}


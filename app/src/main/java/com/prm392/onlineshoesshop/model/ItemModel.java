package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class ItemModel implements Parcelable {


    private String itemId;
    private String title;
    private String description;
    private List<String> picUrl;
    private List<String> size;
    private Double price;
    private Double rating;
    private Integer numberInCart;
    private String brand;

    public ItemModel() {
    }

    public ItemModel(String itemId, String title, String description, List<String> picUrl, List<String> size, Double price, Double rating, Integer numberInCart, String brand) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.picUrl = picUrl;
        this.size = size;
        this.price = price;
        this.rating = rating;
        this.numberInCart = numberInCart;
        this.brand = brand;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPicUrl() {
        return picUrl;
    }

    public List<String> getSize() {
        return size;
    }

    public Double getPrice() {
        return price;
    }

    public Double getRating() {
        return rating;
    }

    public Integer getNumberInCart() {
        return numberInCart;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setNumberInCart(Integer numberInCart) {
        this.numberInCart = numberInCart;
    }
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    protected ItemModel(Parcel in) {
        itemId = in.readString();
        title = in.readString();
        description = in.readString();
        picUrl = in.createStringArrayList();
        size = in.createStringArrayList();
        brand = in.readString();

        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readDouble();
        }
        if (in.readByte() == 0) {
            rating = null;
        } else {
            rating = in.readDouble();
        }
        if (in.readByte() == 0) {
            numberInCart = null;
        } else {
            numberInCart = in.readInt();
        }
    }

    public static final Creator<ItemModel> CREATOR = new Creator<>() {
        @Override
        public ItemModel createFromParcel(Parcel in) {
            return new ItemModel(in);
        }

        @Override
        public ItemModel[] newArray(int size) {
            return new ItemModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeStringList(picUrl);
        dest.writeStringList(size);
        dest.writeString(brand);


        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }

        if (rating == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(rating);
        }

        if (numberInCart == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(numberInCart);
        }
    }
}

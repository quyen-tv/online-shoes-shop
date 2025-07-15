package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class CartItem implements Parcelable {
    private ItemModel item;
    private String selectedSize;
    private int quantity;

    public CartItem(ItemModel item, String selectedSize, int quantity) {
        this.item = item;
        this.selectedSize = selectedSize;
        this.quantity = quantity;
    }

    protected CartItem(Parcel in) {
        item = in.readParcelable(ItemModel.class.getClassLoader());
        selectedSize = in.readString();
        quantity = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(item, flags);
        dest.writeString(selectedSize);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ItemModel getItem() {
        return item;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

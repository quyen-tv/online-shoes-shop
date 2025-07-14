package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionItem implements Parcelable {
    private String itemId;
    private String name;
    private Double price;
    private int quantity;
    private String size;  // ✅ thêm thuộc tính size (kiểu chuỗi)
    private String picUrl; // ✅ Thêm đường dẫn ảnh

    public TransactionItem() {}

    public TransactionItem(String itemId, String name, int quantity, double price, String size, String picUrl) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.size = size;
        this.picUrl = picUrl;
    }


    // Getter & Setter
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    // Getter & Setter
    public String getPicUrl() { return picUrl; }
    public void setPicUrl(String picUrl) { this.picUrl = picUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    // Parcelable
    protected TransactionItem(Parcel in) {
        itemId = in.readString();
        name = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        size = in.readString();
        picUrl = in.readString(); // ✅ đọc picUrl
    }


    public static final Creator<TransactionItem> CREATOR = new Creator<TransactionItem>() {
        @Override
        public TransactionItem createFromParcel(Parcel in) {
            return new TransactionItem(in);
        }

        @Override
        public TransactionItem[] newArray(int size) {
            return new TransactionItem[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeString(size);
        dest.writeString(picUrl); // ✅ ghi picUrl
    }


    @Override
    public int describeContents() {
        return 0;
    }
}

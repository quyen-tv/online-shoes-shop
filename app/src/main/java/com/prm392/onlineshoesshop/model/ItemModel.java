package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemModel implements Parcelable {

    private String itemId;
    private String title;
    private String description;
    private List<String> picUrl;
    private List<StockEntry> stockEntries;
    private Double price;
    private Double rating;
    private Integer numberInCart;
    private String brand;
    private String category;
    private String color;
    private String features;
    private String targetUser;
    private Integer sold;

    public ItemModel() {
    }

    public ItemModel(String itemId, String title, String description, List<String> picUrl,
            List<StockEntry> stockEntries, Double price, Double rating, Integer numberInCart, String brand,
            String category, String color, String features, String targetUser, Integer sold) {
        this.itemId = itemId;
        this.title = title;
        this.description = description;
        this.picUrl = picUrl;
        this.stockEntries = stockEntries;
        this.price = price;
        this.rating = rating;
        this.numberInCart = numberInCart;
        this.brand = brand;
        this.category = category;
        this.color = color;
        this.features = features;
        this.targetUser = targetUser;
        this.sold = sold;
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
    public void setTitle(String title){this.title= title;}
    public String getDescription() {
        return description;
    }

    public List<String> getPicUrl() {
        return picUrl;
    }
    public void setPicUrl(List<String> picUrl){ this.picUrl = picUrl;}
    public List<StockEntry> getStockEntries() {
        return stockEntries;
    }

    public void setStockEntries(List<StockEntry> stockEntries) {
        this.stockEntries = stockEntries;
    }

    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price){this.price=price;}
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public Integer getSold() {
        return sold;
    }

    public void setSold(Integer sold) {
        this.sold = sold;
    }

    protected ItemModel(Parcel in) {
        itemId = in.readString();
        title = in.readString();
        description = in.readString();
        picUrl = in.createStringArrayList();
        stockEntries = new ArrayList<>();
        in.readTypedList(stockEntries, StockEntry.CREATOR);
        brand = in.readString();
        category = in.readString();
        color = in.readString();
        features = in.readString();
        targetUser = in.readString();
        sold = in.readByte() == 0 ? null : in.readInt();
        price = in.readByte() == 0 ? null : in.readDouble();
        rating = in.readByte() == 0 ? null : in.readDouble();
        numberInCart = in.readByte() == 0 ? null : in.readInt();
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
        dest.writeTypedList(stockEntries);
        dest.writeString(brand);
        dest.writeString(category);
        dest.writeString(color);
        dest.writeString(features);
        dest.writeString(targetUser);
        if (sold == null)
            dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(sold);
        }
        if (price == null)
            dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        if (rating == null)
            dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeDouble(rating);
        }
        if (numberInCart == null)
            dest.writeByte((byte) 0);
        else {
            dest.writeByte((byte) 1);
            dest.writeInt(numberInCart);
        }
    }

    public static class StockEntry implements Parcelable {
        private String size;
        private int quantity;

        public StockEntry() {
        }

        public StockEntry(String size, int quantity) {
            this.size = size;
            this.quantity = quantity;
        }

        public String getSize() {
            return size;
        }

        public int getQuantity() {
            return quantity;
        }

        protected StockEntry(Parcel in) {
            size = in.readString();
            quantity = in.readInt();
        }

        public static final Creator<StockEntry> CREATOR = new Creator<>() {
            @Override
            public StockEntry createFromParcel(Parcel in) {
                return new StockEntry(in);
            }

            @Override
            public StockEntry[] newArray(int size) {
                return new StockEntry[size];
            }
        };

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(size);
            dest.writeInt(quantity);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    public Map<String, Integer> getSizeQuantityMap() {
        Map<String, Integer> sizeMap = new LinkedHashMap<>(); // Giữ thứ tự như trong danh sách
        if (stockEntries != null) {
            for (StockEntry entry : stockEntries) {
                sizeMap.put(entry.getSize(), entry.getQuantity());
            }
        }
        return sizeMap;
    }

    public int getStockForSize(String size) {
        if (stockEntries == null)
            return 0;
        for (StockEntry entry : stockEntries) {
            if (entry.getSize().equals(size))
                return entry.getQuantity();
        }
        return 0;
    }

}

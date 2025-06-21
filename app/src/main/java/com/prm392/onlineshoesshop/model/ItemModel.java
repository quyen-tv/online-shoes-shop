package com.prm392.onlineshoesshop.model;

import java.util.List;

public class ItemModel {

    private String title;
    private String description;
    private List<String> picUrl;
    private List<String> size;
    private Double price;
    private Double rating;
    private Integer numberInCart;

    public ItemModel() {
    }

    public ItemModel(String title, String description, List<String> picUrl, List<String> size, Double price, Double rating, Integer numberInCart) {
        this.title = title;
        this.description = description;
        this.picUrl = picUrl;
        this.size = size;
        this.price = price;
        this.rating = rating;
        this.numberInCart = numberInCart;
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
}

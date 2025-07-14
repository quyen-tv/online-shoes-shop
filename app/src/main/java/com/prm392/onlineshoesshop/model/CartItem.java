package com.prm392.onlineshoesshop.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private ItemModel item;
    private String selectedSize;
    private int quantity;

    public CartItem(ItemModel item, String selectedSize, int quantity) {
        this.item = item;
        this.selectedSize = selectedSize;
        this.quantity = quantity;
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

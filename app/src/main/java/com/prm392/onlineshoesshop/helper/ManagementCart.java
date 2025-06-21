package com.prm392.onlineshoesshop.helper;

import android.content.Context;
import android.widget.Toast;

import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.ArrayList;

public class ManagementCart {

    private Context context;
    private TinyDB tinyDB;

    public ManagementCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    public void insertFood(ItemModel item) {
        ArrayList<ItemModel> listFood = getListCart(); // Get current cart list
        boolean existAlready = false;
        int index = -1;

        // Find if item already exists and its index
        for (int i = 0; i < listFood.size(); i++) {
            if (listFood.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                index = i;
                break;
            }
        }

        if (existAlready) {
            // Update the numberInCart for the existing item
            listFood.get(index).setNumberInCart(item.getNumberInCart());
        } else {
            // Add the new item to the list
            listFood.add(item);
        }

        tinyDB.putListObject("CartList", listFood); // Save updated list
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<ItemModel> getListCart() {
        // getListObject returns ArrayList<Object>, so we cast it to ArrayList<ItemModel>
        // It might return null if "CartList" doesn't exist, so we handle that case.
        ArrayList<ItemModel> list = tinyDB.getListObject("CartList"); // Assuming TinyDB has a method that takes a class
        if (list == null) {
            return new ArrayList<>(); // Return an empty list if nothing found
        }
        return list;
    }

    public void minusItem(ArrayList<ItemModel> listFood, int position, ChangeNumberItemsListener listener) {
        if (listFood.get(position).getNumberInCart() == 1) {
            listFood.remove(position); // Remove item if count is 1
        } else {
            listFood.get(position).setNumberInCart(listFood.get(position).getNumberInCart() - 1); // Decrement count
        }
        tinyDB.putListObject("CartList", listFood);
        listener.onChanged(); // Notify listener
    }

    public void plusItem(ArrayList<ItemModel> listFood, int position, ChangeNumberItemsListener listener) {
        listFood.get(position).setNumberInCart(listFood.get(position).getNumberInCart() + 1); // Increment count
        tinyDB.putListObject("CartList", listFood);
        listener.onChanged(); // Notify listener
    }

    public double getTotalFee() {
        ArrayList<ItemModel> listFood = getListCart();
        double fee = 0.0;
        for (ItemModel item : listFood) {
            fee += (item.getPrice() * item.getNumberInCart());
        }
        return fee;
    }

}

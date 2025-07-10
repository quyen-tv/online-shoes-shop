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

    public void insertItem(ItemModel item) {
        ArrayList<ItemModel> itemList = getItemList();
        boolean existAlready = false;
        int index = -1;

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                index = i;
                break;
            }
        }

        if (existAlready) {
            itemList.get(index).setNumberInCart(item.getNumberInCart());
        } else {
            itemList.add(item);
        }

        tinyDB.putListObject("CartList", itemList);
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<ItemModel> getItemList() {
        ArrayList<ItemModel> itemList = tinyDB.getListObject("CartList");
        return (itemList != null) ? itemList : new ArrayList<>();
    }

    public void minusItem(ArrayList<ItemModel> itemList, int position, ChangeNumberItemsListener listener) {
        if (itemList.get(position).getNumberInCart() == 1) {
            itemList.remove(position);
        } else {
            itemList.get(position).setNumberInCart(itemList.get(position).getNumberInCart() - 1);
        }
        tinyDB.putListObject("CartList", itemList);
        listener.onChanged();
    }

    public void plusItem(ArrayList<ItemModel> itemList, int position, ChangeNumberItemsListener listener) {
        itemList.get(position).setNumberInCart(itemList.get(position).getNumberInCart() + 1);
        tinyDB.putListObject("CartList", itemList);
        listener.onChanged();
    }

    public double getTotalFee() {
        ArrayList<ItemModel> itemList = getItemList();
        double fee = 0.0;
        for (ItemModel item : itemList) {
            fee += (item.getPrice() * item.getNumberInCart());
        }
        return fee;
    }

    public void clearCart() {
        tinyDB.remove("CartList");
    }
}

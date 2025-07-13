package com.prm392.onlineshoesshop.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.prm392.onlineshoesshop.model.CartItem;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.ArrayList;

public class ManagementCart {

    private Context context;
    private TinyDB tinyDB;

    public ManagementCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
    }

    public void insertItem(ItemModel item, String selectedSize, int quantity) {
        ArrayList<CartItem> cartList = getCartItems();
        boolean existAlready = false;
        int index = -1;

        for (int i = 0; i < cartList.size(); i++) {
            CartItem ci = cartList.get(i);
            if (ci.getItem().getItemId().equals(item.getItemId())
                    && ci.getSelectedSize().equals(selectedSize)) {
                existAlready = true;
                index = i;
                break;
            }
        }

        if (existAlready) {
            cartList.get(index).setQuantity(quantity);
        } else {
            cartList.add(new CartItem(item, selectedSize, quantity));
        }

        tinyDB.putCartItemList("CartList", cartList);
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show();
    }

    public ArrayList<CartItem> getCartItems() {
        ArrayList<CartItem> cartList = tinyDB.getCartItemList("CartList");
        if (cartList == null) {
            Log.w("ManagementCart", "cartList is null, returning empty list");
            return new ArrayList<>();
        }
        return cartList;
    }


    public boolean increaseQuantity(CartItem targetItem) {
        ArrayList<CartItem> cartList = getCartItems();
        for (CartItem ci : cartList) {
            if (ci.getItem().getItemId().equals(targetItem.getItem().getItemId())
                    && ci.getSelectedSize().equals(targetItem.getSelectedSize())) {

                for (ItemModel.StockEntry entry : ci.getItem().getStockEntries()) {
                    if (entry.getSize().equals(ci.getSelectedSize())) {
                        int maxStock = entry.getQuantity();
                        if (ci.getQuantity() < maxStock) {
                            ci.setQuantity(ci.getQuantity() + 1);
                            saveCartItems(cartList); // ✅ Lưu danh sách đã sửa
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }


    public boolean decreaseQuantity(CartItem targetItem) {
        ArrayList<CartItem> cartList = getCartItems();
        for (CartItem ci : cartList) {
            if (ci.getItem().getItemId().equals(targetItem.getItem().getItemId())
                    && ci.getSelectedSize().equals(targetItem.getSelectedSize())) {

                if (ci.getQuantity() > 1) {
                    ci.setQuantity(ci.getQuantity() - 1);
                    saveCartItems(cartList); // ✅ Lưu danh sách đã sửa
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }



    public double getTotalFee() {
        ArrayList<CartItem> cartList = getCartItems();
        double fee = 0.0;
        for (CartItem ci : cartList) {
            fee += (ci.getItem().getPrice() * ci.getQuantity());
        }
        return fee;
    }

    public void clearCart() {
        tinyDB.remove("CartList");
    }

    public void saveCartItems(ArrayList<CartItem> cartItems) {
        tinyDB.putCartItemList("CartList", cartItems);
    }

}


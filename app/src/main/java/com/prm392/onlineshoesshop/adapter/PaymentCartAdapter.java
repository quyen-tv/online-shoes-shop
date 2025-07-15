// File: PaymentCartAdapter.java
package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.CartItem;

import java.util.List;

public class PaymentCartAdapter extends RecyclerView.Adapter<PaymentCartAdapter.CartViewHolder> {
    private final Context context;
    private final List<CartItem> cartItems;

    public PaymentCartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.name.setText(cartItem.getItem().getTitle());
        holder.size.setText("Size: " + cartItem.getSelectedSize());
        holder.quantity.setText("x" + cartItem.getQuantity());

        List<String> images = cartItem.getItem().getPicUrl();
        if (images != null && !images.isEmpty()) {
            Glide.with(context).load(images.get(0)).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, size, quantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemPaymentImage);
            name = itemView.findViewById(R.id.itemPaymentName);
            size = itemView.findViewById(R.id.itemPaymentSize);
            quantity = itemView.findViewById(R.id.itemPaymentQuantity);
        }
    }
}

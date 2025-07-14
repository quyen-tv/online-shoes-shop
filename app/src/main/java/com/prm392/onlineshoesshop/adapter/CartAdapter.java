package com.prm392.onlineshoesshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.databinding.ViewholderCartBinding;
import com.prm392.onlineshoesshop.helper.ChangeNumberItemsListener;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.CartItem;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final ArrayList<CartItem> cartItems;
    private final ManagementCart managementCart;
    private ChangeNumberItemsListener changeNumberItemsListener;

    public CartAdapter(ArrayList<CartItem> cartItems, Context context, ChangeNumberItemsListener listener) {
        this.cartItems = cartItems;
        this.managementCart = new ManagementCart(context);
        this.changeNumberItemsListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCartBinding binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        ItemModel item = cartItem.getItem();

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.sizeTxt.setText("Size: " + cartItem.getSelectedSize());
        holder.binding.feeEachItem.setText("$" + item.getPrice());
        holder.binding.totalEachItem.setText("$" + Math.round(item.getPrice() * cartItem.getQuantity()));
        holder.binding.numberItemTxt.setText(String.valueOf(cartItem.getQuantity()));

        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.pic);

        holder.binding.plusCartBtn.setOnClickListener(v -> {
            int currentQty = cartItem.getQuantity();
            int maxStock = cartItem.getItem().getStockForSize(cartItem.getSelectedSize());

            if (currentQty < maxStock) {
                cartItem.setQuantity(currentQty + 1);
                managementCart.saveCartItems(cartItems); // Cập nhật TinyDB
                notifyItemChanged(position);
                if (changeNumberItemsListener != null) changeNumberItemsListener.onChanged();
            } else {
                Toast.makeText(holder.itemView.getContext(), "Vượt quá số lượng trong kho", Toast.LENGTH_SHORT).show();
            }
        });

        holder.binding.minusCartBtn.setOnClickListener(v -> {
            int currentQty = cartItem.getQuantity();
            if (currentQty > 1) {
                cartItem.setQuantity(currentQty - 1);
                managementCart.saveCartItems(cartItems);
                notifyItemChanged(position);
            } else {
                cartItems.remove(position);
                managementCart.saveCartItems(cartItems);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
            }
            if (changeNumberItemsListener != null) changeNumberItemsListener.onChanged();
        });




    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * A [RecyclerView.ViewHolder] that holds the brand item layout.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderCartBinding binding;

        public ViewHolder(ViewholderCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
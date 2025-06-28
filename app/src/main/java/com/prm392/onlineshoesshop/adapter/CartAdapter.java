package com.prm392.onlineshoesshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.databinding.ViewholderCartBinding;
import com.prm392.onlineshoesshop.helper.ChangeNumberItemsListener;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final ArrayList<ItemModel> listItemSelected;
    private final ManagementCart managementCart;
    private ChangeNumberItemsListener changeNumberItemsListener;

    public CartAdapter(ArrayList<ItemModel> listItemSelected, Context context, ChangeNumberItemsListener changeNumberItemsListener) {
        this.listItemSelected = listItemSelected;
        this.managementCart = new ManagementCart(context);
        this.changeNumberItemsListener = changeNumberItemsListener;
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
        ItemModel item = listItemSelected.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.feeEachItem.setText("$" + item.getPrice());
        holder.binding.totalEachItem.setText("$" + Math.round(item.getPrice() * item.getNumberInCart()));
        holder.binding.numberItemTxt.setText(String.valueOf(item.getNumberInCart()));

        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.pic);

        holder.binding.plusCartBtn.setOnClickListener(v -> managementCart.plusItem(listItemSelected, position, new ChangeNumberItemsListener() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
                if (changeNumberItemsListener != null) {
                    changeNumberItemsListener.onChanged();
                }
            }
        }));

        holder.binding.minusCartBtn.setOnClickListener(v -> managementCart.minusItem(listItemSelected, position, new ChangeNumberItemsListener() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
                if (changeNumberItemsListener != null) {
                    changeNumberItemsListener.onChanged();
                }
            }
        }));
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
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
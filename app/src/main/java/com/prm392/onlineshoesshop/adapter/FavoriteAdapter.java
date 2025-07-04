package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ItemFavoriteBinding;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private final List<ItemModel> favoriteList;
    private final ItemViewModel itemViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClicked(ItemModel item);
    }

    public FavoriteAdapter(LifecycleOwner lifecycleOwner, List<ItemModel> favoriteList,
                           ItemViewModel itemViewModel, OnItemClickListener listener) {
        this.lifecycleOwner = lifecycleOwner;
        this.favoriteList = favoriteList;
        this.itemViewModel = itemViewModel;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteBinding binding = ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        ItemModel item = favoriteList.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvItemName.setText(item.getTitle());
        holder.binding.tvItemPrice.setText(String.format("$%.2f", item.getPrice()));

        String itemId = item.getItemId();
        if (itemId != null && !itemId.isEmpty()) {
            itemViewModel.isItemFavorite(itemId).observe(lifecycleOwner, isFavorite -> {
                int iconRes = Boolean.TRUE.equals(isFavorite) ? R.drawable.fav_icon_fill : R.drawable.fav_icon;
                holder.binding.btnFavorite.setImageResource(iconRes);
            });

            holder.binding.btnFavorite.setOnClickListener(v -> {

                itemViewModel.toggleFavorite(itemId);
            });
        } else {
            holder.binding.btnFavorite.setImageResource(R.drawable.fav_icon); // default icon
            holder.binding.btnFavorite.setOnClickListener(v -> {
                Log.d("Favor", "this itemId: " + itemId );
            }); // hoặc disable
        }


        // Load ảnh sản phẩm
        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.imgItem);


        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClicked(item);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }



    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteBinding binding;

        public FavoriteViewHolder(ItemFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

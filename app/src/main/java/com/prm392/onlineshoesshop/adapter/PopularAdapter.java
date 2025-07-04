package com.prm392.onlineshoesshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private final List<ItemModel> items;
    private Context context;

    private List<String> favoriteIds = new ArrayList<>();

    private OnChangeListener listener;

    public void setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public interface OnChangeListener {
        void onToggleFavorite(String itemId);
        void onClick(ItemModel item);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    public PopularAdapter(List<ItemModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PopularAdapter.PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderRecommendedBinding binding = ViewholderRecommendedBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PopularViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.PopularViewHolder holder, int position) {
        ItemModel item = items.get(position);
        context = holder.itemView.getContext();

        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvPrice.setText(String.format("$%s", item.getPrice()));
        holder.binding.tvRating.setText(String.valueOf(item.getRating()));
        boolean isFavorite = favoriteIds.contains(ItemUtils.getFirebaseItemId(item.getItemId()));
        if (isFavorite) {
            holder.binding.imgFavorite.setImageResource(R.drawable.ic_fav_fill);
            holder.binding.imgFavorite.setColorFilter(null);
        } else {
            holder.binding.imgFavorite.setImageResource(R.drawable.ic_fav);
            holder.binding.imgFavorite.setColorFilter(
                    ContextCompat.getColor(context, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN
            );
        }

        RequestOptions options = new RequestOptions().transform(new CenterCrop());
        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(options)
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));

        holder.binding.imgFavorite.setOnClickListener(v -> listener.onToggleFavorite(item.getItemId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class PopularViewHolder extends RecyclerView.ViewHolder {
        ViewholderRecommendedBinding binding;
        public PopularViewHolder(ViewholderRecommendedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

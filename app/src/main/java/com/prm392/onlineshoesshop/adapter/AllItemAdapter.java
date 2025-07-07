package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.activity.DetailActivity;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.ItemModel;

import androidx.core.content.ContextCompat;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.utils.ItemUtils;
import java.util.ArrayList;
import java.util.List;

public class AllItemAdapter extends RecyclerView.Adapter<AllItemAdapter.AllItemViewHolder> {

    private List<ItemModel> items;
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

    @android.annotation.SuppressLint("NotifyDataSetChanged")
    public void setFavoriteIds(List<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    public AllItemAdapter(List<ItemModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public AllItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderRecommendedBinding binding = ViewholderRecommendedBinding.inflate(LayoutInflater.from(context),
                parent, false);
        return new AllItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AllItemViewHolder holder, int position) {
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
            else context.startActivity(new Intent(context, DetailActivity.class).putExtra("object", item));
        });

        holder.binding.imgFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onToggleFavorite(item.getItemId());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class AllItemViewHolder extends RecyclerView.ViewHolder {
        ViewholderRecommendedBinding binding;

        public AllItemViewHolder(ViewholderRecommendedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void updateData(List<ItemModel> newData) {
        this.items = newData;
        notifyDataSetChanged();
    }

}

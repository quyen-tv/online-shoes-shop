package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.activity.DetailActivity;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private final List<ItemModel> items;
    private final ItemViewModel itemViewModel;
    private final LifecycleOwner lifecycleOwner;
    private final ActivityResultLauncher<Intent> launcher;

    public PopularAdapter(List<ItemModel> items, ItemViewModel itemViewModel,
                          LifecycleOwner lifecycleOwner, ActivityResultLauncher<Intent> launcher) {
        this.items = items;
        this.itemViewModel = itemViewModel;
        this.lifecycleOwner = lifecycleOwner;
        this.launcher = launcher;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderRecommendedBinding binding = ViewholderRecommendedBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PopularViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, int position) {
        ItemModel item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvPrice.setText(String.format("$%s", item.getPrice()));
        holder.binding.tvRating.setText(String.valueOf(item.getRating()));

        // Quan sát trạng thái yêu thích
        itemViewModel.isItemFavorite(item.getItemId()).observe(lifecycleOwner, isFavorite -> {
            int iconRes = Boolean.TRUE.equals(isFavorite) ? R.drawable.fav_icon_fill : R.drawable.fav_icon;
            holder.binding.imgFavorite.setImageResource(iconRes);
        });

        // Xử lý toggle yêu thích
        holder.binding.imgFavorite.setOnClickListener(v -> {
            itemViewModel.toggleFavorite(item.getItemId());
        });

        // Load ảnh sản phẩm
        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.binding.pic);

        // Mở DetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", item);
            launcher.launch(intent);
        });
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

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

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private List<ItemModel> items;
    private Context context;

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
        Context context = holder.itemView.getContext();

        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvPrice.setText(String.format("$%s", item.getPrice()));
        holder.binding.tvRating.setText(String.valueOf(item.getRating()));

        RequestOptions options = new RequestOptions().transform(new CenterCrop());
        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(options)
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(v -> {
            context.startActivity(new Intent(context, DetailActivity.class).putExtra("object", item));
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

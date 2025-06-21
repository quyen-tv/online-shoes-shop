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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.SliderModel;

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
        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvPrice.setText(String.format("$%s", item.getPrice()));
        holder.binding.tvRating.setText(String.valueOf(item.getRating()));

        RequestOptions options = new RequestOptions().transform(new CenterCrop());
        Glide.with(holder.itemView.getContext())
                .load(item.getPicUrl().get(0))
                .apply(options)
                .into(holder.binding.pic);
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

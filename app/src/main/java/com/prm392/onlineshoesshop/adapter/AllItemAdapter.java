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

public class AllItemAdapter extends RecyclerView.Adapter<AllItemAdapter.AllItemViewHolder> {

    private final List<ItemModel> items;
    private Context context;

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

        RequestOptions options = new RequestOptions().transform(new CenterCrop());
        Glide.with(context)
                .load(item.getPicUrl().get(0))
                .apply(options)
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(
                v -> context.startActivity(new Intent(context, DetailActivity.class).putExtra("object", item)));
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
}

package com.prm392.onlineshoesshop.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.activity.DetailActivity;
import com.prm392.onlineshoesshop.databinding.ViewholderCategoryBinding;
import com.prm392.onlineshoesshop.databinding.ViewholderRecommendedBinding;
import com.prm392.onlineshoesshop.model.CategoryModel;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryModel> items;
    private Context context;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;

    public CategoryAdapter(List<CategoryModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderCategoryBinding binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CategoryViewHolder(binding);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        CategoryModel item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.binding.tvTitle.setText(item.getTitle());

        RequestOptions options = new RequestOptions().transform(new CenterCrop());
        Glide.with(context)
                .load(item.getPicUrl())
                .apply(options)
                .into(holder.binding.pic);

        // Set the click listener for the item view
        holder.binding.getRoot().setOnClickListener(v -> {
            lastSelectedPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);
        });

        holder.binding.tvTitle.setTextColor(R.color.white);

        // Change the item's appearance based on whether it is selected
        if (selectedPosition == position) {
            // --- Style for the SELECTED item ---
            holder.binding.pic.setBackgroundColor(0); // Set background to transparent
            holder.binding.mainLayout.setBackgroundResource(R.drawable.purple_button_bg);
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
            ImageViewCompat.setImageTintList(holder.binding.pic, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
            holder.binding.tvTitle.setVisibility(View.VISIBLE);
        } else {
            // --- Style for an UNSELECTED item ---
            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.mainLayout.setBackgroundResource(0); // Remove background
            holder.binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.black)); // Assuming black is the default
            ImageViewCompat.setImageTintList(holder.binding.pic, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
            holder.binding.tvTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ViewholderCategoryBinding binding;
        public CategoryViewHolder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}


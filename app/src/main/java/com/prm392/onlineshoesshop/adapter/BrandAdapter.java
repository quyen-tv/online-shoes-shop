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
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.Brand;

import java.util.ArrayList;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.brandImage);
            name = itemView.findViewById(R.id.brandName);
        }
    }

    private List<Brand> mBrands;

    public BrandAdapter() {
        this.mBrands = new ArrayList<>();
    }

    public void updateBrands(List<Brand> brands) {
        this.mBrands = brands;
        notifyDataSetChanged(); // This is crucial! It tells the RecyclerView to refresh.
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View brandView = inflater.inflate(R.layout.brand_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(brandView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Brand brand = mBrands.get(position);

        TextView textView = holder.name;
        textView.setText(brand.getTitle());
        ImageView imageView = holder.image;

        // Use Glide to load the image from the URL
        Glide.with(holder.itemView.getContext()) // Get context from the holder's view
                .load(brand.getPicUrl())
                .into(imageView); // Target the correct ImageView
    }

    @Override
    public int getItemCount() {
        return mBrands.size();
    }
}

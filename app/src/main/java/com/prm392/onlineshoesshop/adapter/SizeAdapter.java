package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ViewholderColorBinding;
import com.prm392.onlineshoesshop.databinding.ViewholderSizeBinding;

import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder> {

    private List<String> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private Context context;

    public SizeAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SizeAdapter.SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderSizeBinding binding = ViewholderSizeBinding.inflate(LayoutInflater.from(context), parent, false);
        return new SizeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeAdapter.SizeViewHolder holder, int position) {
        String item = items.get(position);

        holder.binding.tvSize.setText(item);

        holder.binding.getRoot().setOnClickListener(v -> {
            lastSelectedPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);
        });

        if (selectedPosition == position) {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg_selected);
            holder.binding.tvSize.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.tvSize.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class SizeViewHolder extends RecyclerView.ViewHolder {
        ViewholderSizeBinding binding;

        public SizeViewHolder(ViewholderSizeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

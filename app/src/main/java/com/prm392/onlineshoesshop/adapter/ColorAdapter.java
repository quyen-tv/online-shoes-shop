package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ViewholderColorBinding;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private List<String> items;

    private int selectedPosition = 0;
    private Context context;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int position);
    }

    public ColorAdapter(List<String> items, OnColorSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorAdapter.ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderColorBinding binding = ViewholderColorBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ColorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorAdapter.ColorViewHolder holder, int position) {
        String item = items.get(position);

        Glide.with(holder.itemView.getContext())
                .load(item)
                .into(holder.binding.pic);

        holder.binding.getRoot().setOnClickListener(v -> {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            ;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onColorSelected(selectedPosition);
            }
        });

        if (selectedPosition == position) {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg_selected);
        } else {
            holder.binding.colorLayout.setBackgroundResource(R.drawable.grey_bg);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setSelectedPosition(int position) {
        if (selectedPosition != position) {
            int previousSelectedPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        ViewholderColorBinding binding;

        public ColorViewHolder(ViewholderColorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

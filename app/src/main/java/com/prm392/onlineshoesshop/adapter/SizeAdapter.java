package com.prm392.onlineshoesshop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ViewholderSizeBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder> {

    private final List<Map.Entry<String, Integer>> items;
    private int selectedPosition = -1;
    private int lastSelectedPosition = -1;
    private Context context;
    private OnSizeSelectedListener onSizeSelectedListener;

    public SizeAdapter(Map<String, Integer> sizeQuantityMap) {
        this.items = new ArrayList<>(sizeQuantityMap.entrySet()); // preserve order
    }

    @NonNull
    @Override
    public SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderSizeBinding binding = ViewholderSizeBinding.inflate(LayoutInflater.from(context), parent, false);
        return new SizeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder holder, int position) {
        Map.Entry<String, Integer> entry = items.get(position);
        String size = entry.getKey();
        int quantity = entry.getValue();

        holder.binding.tvSize.setText(size);

        boolean isOutOfStock = quantity <= 0;
        boolean isSelected = selectedPosition == position;

        // Không cho chọn size hết hàng
        holder.binding.getRoot().setEnabled(!isOutOfStock);
        holder.binding.tvSize.setAlpha(isOutOfStock ? 0.4f : 1.0f);

        if (isSelected) {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg_selected);
            holder.binding.tvSize.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            holder.binding.sizeLayout.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.tvSize.setTextColor(context.getResources().getColor(R.color.black));
        }

        // Xử lý click nếu còn hàng
        if (!isOutOfStock) {
            holder.binding.getRoot().setOnClickListener(v -> {
                lastSelectedPosition = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);
                if (onSizeSelectedListener != null) {
                    onSizeSelectedListener.onSizeSelected(getSelectedSize());
                }
            });
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

    // ✅ Trả về size đã chọn
    public String getSelectedSize() {
        if (selectedPosition >= 0 && selectedPosition < items.size()) {
            return items.get(selectedPosition).getKey();
        }
        return null;
    }

    public void setOnSizeSelectedListener(OnSizeSelectedListener listener) {
        this.onSizeSelectedListener = listener;
    }

    public interface OnSizeSelectedListener {
        void onSizeSelected(String selectedSize);
    }
}

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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.TransactionItem;

import java.util.List;

public class TransactionDetailAdapter extends RecyclerView.Adapter<TransactionDetailAdapter.ViewHolder> {

    private final List<TransactionItem> items;
    private final Context context;

    public TransactionDetailAdapter(List<TransactionItem> items, Context context) {
        this.items = items;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = items.get(position);

        holder.itemTitle.setText(item.getName());
        holder.itemQuantity.setText("Qty: " + item.getQuantity());
        holder.itemPrice.setText("$" + String.format("%.2f", item.getPrice()));
        holder.itemSize.setText("Size: " + item.getSize()); // ✅ Hiển thị size

        // Load image
        Glide.with(context)
                .load(item.getPicUrl())
                .apply(new RequestOptions().transform(new CenterCrop()))
                .into(holder.itemImage);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle, itemQuantity, itemPrice, itemSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemPaymentImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemQuantity = itemView.findViewById(R.id.itemPaymentQuantity);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemSize = itemView.findViewById(R.id.itemPaymentSize);

        }
    }
}

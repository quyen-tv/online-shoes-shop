package com.prm392.onlineshoesshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final Context context;

    public TransactionAdapter(List<Transaction> transactionList, Context context) {
        this.transactionList = transactionList;
        this.context = context;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_history, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.tvTransactionId.setText("ID: " + transaction.getAppTransId());
        holder.tvTimestamp.setText(formatDate(transaction.getCreatedAt()));
        holder.tvTotalAmount.setText("Total: $" + transaction.getTotalAmount());
        holder.tvMethod.setText("Method: " + transaction.getPaymentMethod());

        // Trạng thái
        Transaction.Status status = transaction.getStatus();
        holder.tvStatus.setText("Status: " + status.name());

        switch (status) {
            case PENDING:
                holder.tvStatus.setTextColor(context.getColor(R.color.orange)); // define in colors.xml
                break;
            case SUCCESS:
                holder.tvStatus.setTextColor(context.getColor(R.color.green)); // define in colors.xml
                break;
            case FAILED:
                holder.tvStatus.setTextColor(context.getColor(R.color.red)); // define in colors.xml
                break;
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionId, tvTimestamp, tvTotalAmount, tvStatus, tvMethod;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMethod = itemView.findViewById(R.id.tvMethod);
        }
    }

    private String formatDate(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

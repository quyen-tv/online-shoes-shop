package com.prm392.onlineshoesshop.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.activity.TransactionDetailActivity;
import com.prm392.onlineshoesshop.model.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
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

        holder.tvTransactionId.setText("Mã đơn: " + transaction.getAppTransId());
        holder.tvTimestamp.setText(formatDate(transaction.getCreatedAt()));

        // Format VNĐ
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedAmount = format.format(transaction.getTotalAmount())+"₫"  ;
        holder.tvTotalAmount.setText("Tổng tiền: " + formattedAmount);

        holder.tvMethod.setText("Phương thức: " + transaction.getPaymentMethod());

        // Trạng thái thanh toán
        Transaction.PaymentStatus paymentStatus = transaction.getPaymentStatus();
        switch (paymentStatus) {
            case PENDING:
                holder.tvPaymentStatus.setText("Thanh toán: Đang xử lý");
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.orange));
                break;
            case SUCCESS:
                holder.tvPaymentStatus.setText("Thanh toán: Thành công");
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.green));
                break;
            case FAILED:
                holder.tvPaymentStatus.setText("Thanh toán: Thất bại");
                holder.tvPaymentStatus.setTextColor(context.getColor(R.color.red));
                break;
        }

        // Trạng thái đơn hàng
        Transaction.OrderStatus orderStatus = transaction.getOrderStatus();
        switch (orderStatus) {
            case WAITING_CONFIRMATION:
                holder.tvOrderStatus.setText("Trạng thái: Chờ xác nhận");
                holder.tvOrderStatus.setTextColor(context.getColor(R.color.purple_700));
                break;
            case WAITING_FOR_PICKUP:
                holder.tvOrderStatus.setText("Trạng thái: Chờ lấy hàng");
                holder.tvOrderStatus.setTextColor(context.getColor(R.color.orange));
                break;
            case DELIVERING:
                holder.tvOrderStatus.setText("Trạng thái: Đang giao");
                holder.tvOrderStatus.setTextColor(context.getColor(R.color.custom_blue));
                break;
            case DELIVERED:
                holder.tvOrderStatus.setText("Trạng thái: Đã giao");
                holder.tvOrderStatus.setTextColor(context.getColor(R.color.green));
                break;
            case CANCELLED:
                holder.tvOrderStatus.setText("Trạng thái: Đã huỷ");
                holder.tvOrderStatus.setTextColor(context.getColor(R.color.red));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionId, tvTimestamp, tvTotalAmount, tvPaymentStatus, tvOrderStatus, tvMethod;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvMethod = itemView.findViewById(R.id.tvPaymentMethod);
        }
    }
    public void setData(List<Transaction> newList) {
        this.transactionList.clear();
        this.transactionList.addAll(newList);
        notifyDataSetChanged();
    }


    private String formatDate(long timestamp) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

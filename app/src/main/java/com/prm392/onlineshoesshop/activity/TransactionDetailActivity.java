package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.TransactionDetailAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityTransactionDetailBinding;
import com.prm392.onlineshoesshop.fragment.CancelOrderDialogFragment;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;
import com.prm392.onlineshoesshop.repository.TransactionRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    private ActivityTransactionDetailBinding binding;
    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        transaction = getIntent().getParcelableExtra("transaction");
        if (transaction == null) {
            Toast.makeText(this, "Error loading transaction details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupRecyclerView();

        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void setupViews() {
        binding.tvTransactionId.setText("" + transaction.getAppTransId());
        Long createdAt = transaction.getCreatedAt();
        binding.tvDate.setText(createdAt != null ? formatDate(createdAt) : "Không rõ");

        binding.tvPaymentMethod.setText("" + transaction.getPaymentMethod());

        // Trạng thái thanh toán
        switch (transaction.getPaymentStatus()) {
            case PENDING:
                binding.tvPaymentStatus.setText("Đang xử lý");
                binding.tvPaymentStatus.setTextColor(getColor(R.color.orange));
                break;
            case SUCCESS:
                binding.tvPaymentStatus.setText("Thành công");
                binding.tvPaymentStatus.setTextColor(getColor(R.color.green));
                break;
            case FAILED:
                binding.tvPaymentStatus.setText("Thất bại");
                binding.tvPaymentStatus.setTextColor(getColor(R.color.red));
                break;
        }

        // Trạng thái đơn hàng
        switch (transaction.getOrderStatus()) {
            case WAITING_CONFIRMATION:
                binding.tvOrderStatus.setText("Chờ xác nhận");
                binding.tvOrderStatus.setTextColor(getColor(R.color.purple_700));
                break;
            case WAITING_FOR_PICKUP:
                binding.tvOrderStatus.setText("Chờ lấy hàng");
                binding.tvOrderStatus.setTextColor(getColor(R.color.orange));
                break;
            case DELIVERING:
                binding.tvOrderStatus.setText("Đang giao");
                binding.tvOrderStatus.setTextColor(getColor(R.color.custom_blue));
                break;
            case DELIVERED:
                binding.tvOrderStatus.setText("Đã giao");
                binding.tvOrderStatus.setTextColor(getColor(R.color.green));
                break;
            case CANCELLED:
                binding.tvOrderStatus.setText("Đã huỷ");
                binding.tvOrderStatus.setTextColor(getColor(R.color.red));
                break;
        }

        // ✅ Format tiền
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        double subtotal = transaction.getTotalAmount() - transaction.getTax() - transaction.getDeliveryFee();
        binding.tvSubtotal.setText(currencyFormat.format(subtotal) + "₫");
        binding.tvTax.setText(currencyFormat.format(transaction.getTax()) + "₫");
        binding.tvDeliveryFee.setText(currencyFormat.format(transaction.getDeliveryFee())+  "₫" );
        binding.tvTotal.setText(currencyFormat.format(transaction.getTotalAmount())  + "₫");

        Long paidAt = transaction.getPaidAt();
        if (transaction.getPaymentStatus() == Transaction.PaymentStatus.SUCCESS && paidAt != null && paidAt > 0) {
            binding.tvPaidAt.setText(formatDate(paidAt));
        } else {
            binding.tvPaidAt.setText("Chưa thanh toán");
        }


        if (transaction.getPaymentStatus() == Transaction.PaymentStatus.PENDING
                && transaction.getOrderStatus() == Transaction.OrderStatus.WAITING_CONFIRMATION) {

            binding.actionButtonsLayout.setVisibility(View.VISIBLE);

            if ("ZaloPay".equalsIgnoreCase(transaction.getPaymentMethod())) {
                binding.btnRetryPayment.setVisibility(View.VISIBLE);
                binding.btnRetryPayment.setOnClickListener(v -> {
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("appTransId", transaction.getAppTransId()); // truyền id
                    startActivity(intent);
                });
            } else {
                binding.btnRetryPayment.setVisibility(View.GONE);
            }

            binding.btnCancelOrder.setVisibility(View.VISIBLE);
            binding.btnCancelOrder.setOnClickListener(v -> {
                CancelOrderDialogFragment dialog = CancelOrderDialogFragment.newInstance(transaction);
                dialog.show(getSupportFragmentManager(), "CancelOrderDialog");
            });

        } else {
            binding.actionButtonsLayout.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        List<TransactionItem> items = transaction.getItems();
        if (items == null) items = List.of();

        TransactionDetailAdapter adapter = new TransactionDetailAdapter(items, this);
        binding.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsRecyclerView.setAdapter(adapter);
    }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(timestamp));
    }

    private void increaseStock(List<TransactionItem> items) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (TransactionItem item : items) {
            String itemId = item.getItemId();
            String size = item.getSize();
            int quantity = item.getQuantity();

            DatabaseReference stockRef = itemsRef.child(itemId).child("stockEntries");

            stockRef.get().addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItemModel.StockEntry entry = snapshot.getValue(ItemModel.StockEntry.class);
                    if (entry != null && size.equals(entry.getSize())) {
                        int newQty = entry.getQuantity() + quantity;
                        snapshot.getRef().child("quantity").setValue(newQty);
                        break;
                    }
                }
            });
        }
    }

    private void decreaseSold(List<TransactionItem> items) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (TransactionItem item : items) {
            String itemId = item.getItemId();
            int quantity = item.getQuantity();

            DatabaseReference itemRef = itemsRef.child(itemId);

            itemRef.child("sold").get().addOnSuccessListener(snap -> {
                Long currentSold = snap.getValue(Long.class);
                long newSold = Math.max((currentSold != null ? currentSold : 0) - quantity, 0);
                itemRef.child("sold").setValue(newSold);
            });
        }
    }

}


package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.TransactionDetailAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityTransactionDetailBinding;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;

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
        binding.tvTransactionId.setText(transaction.getAppTransId());
        binding.tvDate.setText(formatDate(transaction.getCreatedAt()));
        binding.tvPaymentMethod.setText(transaction.getPaymentMethod());

        binding.tvPaymentStatus.setText(transaction.getPaymentStatus().name());
        binding.tvOrderStatus.setText(transaction.getOrderStatus().name());

        switch (transaction.getPaymentStatus()) {
            case PENDING:
                binding.tvPaymentStatus.setTextColor(getColor(R.color.orange));
                break;
            case SUCCESS:
                binding.tvPaymentStatus.setTextColor(getColor(R.color.green));
                break;
            case FAILED:
                binding.tvPaymentStatus.setTextColor(getColor(R.color.red));
                break;
        }

        switch (transaction.getOrderStatus()) {
            case WAITING_CONFIRMATION:
                binding.tvOrderStatus.setTextColor(getColor(R.color.purple_700));
                break;
            case WAITING_FOR_PICKUP:
                binding.tvOrderStatus.setTextColor(getColor(R.color.orange));
                break;
            case DELIVERING:
                binding.tvOrderStatus.setTextColor(getColor(R.color.custom_blue));
                break;
            case DELIVERED:
                binding.tvOrderStatus.setTextColor(getColor(R.color.green));
                break;
            case CANCELLED:
                binding.tvOrderStatus.setTextColor(getColor(R.color.red));
                break;
        }

        // ✅ Format tiền
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        double subtotal = transaction.getTotalAmount() - transaction.getTax() - transaction.getDeliveryFee();
        binding.tvSubtotal.setText("₫" + currencyFormat.format(subtotal));
        binding.tvTax.setText("₫" + currencyFormat.format(transaction.getTax()));
        binding.tvDeliveryFee.setText("₫" + currencyFormat.format(transaction.getDeliveryFee()));
        binding.tvTotal.setText("₫" + currencyFormat.format(transaction.getTotalAmount()));

        if (transaction.getPaymentStatus() == Transaction.PaymentStatus.PENDING) {
            binding.actionButtonsLayout.setVisibility(View.VISIBLE);

            binding.btnRetryPayment.setOnClickListener(v -> {
                Toast.makeText(this, "Chức năng thanh toán lại đang được phát triển", Toast.LENGTH_SHORT).show();
            });

            binding.btnCancelOrder.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Xác nhận huỷ đơn")
                        .setMessage("Bạn có chắc muốn huỷ đơn hàng này không?")
                        .setPositiveButton("Huỷ đơn", (dialog, which) -> {
                            Toast.makeText(this, "Đã huỷ đơn (giả lập)", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Không", null)
                        .show();
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
}


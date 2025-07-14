package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.TransactionDetailAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityTransactionDetailBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDetailActivity extends AppCompatActivity {

    private ActivityTransactionDetailBinding binding;
    private Transaction transaction;
    private TransactionDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get transaction from intent
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
        // Transaction info
        binding.tvTransactionId.setText(transaction.getAppTransId());
        binding.tvStatus.setText(transaction.getStatus().name());
        binding.tvDate.setText(formatDate(transaction.getCreatedAt()));
        binding.tvPaymentMethod.setText(transaction.getPaymentMethod());

        // Set status color
        switch (transaction.getStatus()) {
            case PENDING:
                binding.tvStatus.setTextColor(getColor(R.color.orange));
                break;
            case SUCCESS:
                binding.tvStatus.setTextColor(getColor(R.color.green));
                break;
            case FAILED:
                binding.tvStatus.setTextColor(getColor(R.color.red));
                break;
        }

        // Calculate subtotal (total - tax - delivery fee)
        double subtotal = transaction.getTotalAmount() - transaction.getTax() - transaction.getDeliveryFee();
        
        // Summary
        binding.tvSubtotal.setText("$" + String.format("%.2f", subtotal));
        binding.tvTax.setText("$" + String.format("%.2f", transaction.getTax()));
        binding.tvDeliveryFee.setText("$" + String.format("%.2f", transaction.getDeliveryFee()));
        binding.tvTotal.setText("$" + String.format("%.2f", transaction.getTotalAmount()));
    }

    private void setupRecyclerView() {
        List<TransactionItem> items = transaction.getItems(); // đúng kiểu
        if (items == null) {
            items = new ArrayList<>();
        }

        if (items == null) items = new ArrayList<>();
        adapter = new TransactionDetailAdapter(items, this);
        binding.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsRecyclerView.setAdapter(adapter);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}

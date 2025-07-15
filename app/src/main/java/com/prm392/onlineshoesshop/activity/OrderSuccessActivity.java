package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivityOrderSuccessBinding;

public class OrderSuccessActivity extends AppCompatActivity {

    private ActivityOrderSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Xử lý nút "Về Trang Chủ"
        binding.btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xoá backstack
            startActivity(intent);
        });

        // (Tuỳ chọn) Nút xem lịch sử giao dịch
        binding.btnViewOrders.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, TransactionHistoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        // Không cho back về lại trang thanh toán
        // hoặc có thể xử lý giống nhấn "Về trang chủ"
    }
}

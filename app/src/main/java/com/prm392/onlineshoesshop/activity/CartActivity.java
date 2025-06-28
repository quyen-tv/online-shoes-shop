package com.prm392.onlineshoesshop.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prm392.onlineshoesshop.adapter.CartAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityCartBinding;
import com.prm392.onlineshoesshop.helper.ChangeNumberItemsListener;
import com.prm392.onlineshoesshop.helper.ManagementCart;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private ManagementCart managementCart;
    private double tax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // enableEdgeToEdge() is not a standard AndroidX Activity method.
        // If you have a custom implementation or it's from a library,
        // you might need to call it here. For standard edge-to-edge,
        // the modern approach is handled via themes and WindowInsets.
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            android.graphics.Insets systemBars = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            }
            return insets;
        });

        managementCart = new ManagementCart(this);

        setVariable();
        initCartList();
        calculateCart();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
    }

    private void initCartList() {
        binding.viewCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        binding.viewCart.setAdapter(new CartAdapter(managementCart.getListCart(), this, new ChangeNumberItemsListener() {
            @Override
            public void onChanged() {
                calculateCart();
            }
        }));

        if (managementCart.getListCart().isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView2.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView2.setVisibility(View.VISIBLE);
        }
    }

    private void calculateCart() {
        double percentTax = 0.02;
        double deliveryFee = 10.0;

        tax = Math.round((managementCart.getTotalFee() * percentTax) * 100.0) / 100.0;

        double total = Math.round((managementCart.getTotalFee() + tax + deliveryFee) * 100.0) / 100.0;
        double itemTotal = Math.round(managementCart.getTotalFee() * 100.0) / 100.0;

        binding.totalFeeTxt.setText("$" + itemTotal);
        binding.taxTxt.setText("$" + tax);
        binding.deliveryTxt.setText("$" + deliveryFee);
        binding.totalTxt.setText("$" + total);
    }
}
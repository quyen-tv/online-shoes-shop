package com.prm392.onlineshoesshop.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.adapter.CartAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityCartBinding;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.CartItem;
import com.prm392.onlineshoesshop.repository.TransactionRepository;

import java.util.ArrayList;



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


        binding.btnCheckOut.setOnClickListener(v -> {
            checkUserInfo();  // gọi kiểm tra trước khi xử lý thanh toán
        });


    }

    private void initCartList() {
        ArrayList<CartItem> cartList = managementCart.getCartItems();

        binding.viewCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.viewCart.setAdapter(new CartAdapter(cartList, this, () -> calculateCart()));

        if (cartList.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollView2.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollView2.setVisibility(View.VISIBLE);
        }
    }


    private void calculateCart() {
        double percentTax = 0.02;
        double itemTotal = managementCart.getTotalFee();

        if (itemTotal == 0) {
            // Giỏ hàng trống
            binding.totalFeeTxt.setText("$0.00");
            binding.taxTxt.setText("$0.00");
            binding.deliveryTxt.setText("$0.00");
            binding.totalTxt.setText("$0.00");
            binding.btnCheckOut.setEnabled(false);
            return;
        }

        // Giỏ hàng có hàng
        binding.btnCheckOut.setEnabled(true);

        tax = Math.round((itemTotal * percentTax) * 100.0) / 100.0;

        double deliveryFee;
        if (itemTotal >= 100.0) {
            deliveryFee = 0.0;
        } else {
            deliveryFee = 10.0;
        }

        double total = Math.round((itemTotal + tax + deliveryFee) * 100.0) / 100.0;

        // Cập nhật UI
        binding.totalFeeTxt.setText("$" + String.format("%.2f", itemTotal));
        binding.taxTxt.setText("$" + String.format("%.2f", tax));
        binding.deliveryTxt.setText("$" + String.format("%.2f", deliveryFee));
        binding.totalTxt.setText("$" + String.format("%.2f", total));
    }
    private void handleCheckOut() {
        ArrayList<CartItem> cartList = managementCart.getCartItems();
        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double itemTotal = managementCart.getTotalFee();
        double tax = Math.round(itemTotal * 0.02 * 100.0) / 100.0;
        double deliveryFee = itemTotal >= 100.0 ? 0.0 : 10.0;
        double totalAmount = Math.round((itemTotal + tax + deliveryFee) * 100.0) / 100.0;

        Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
        intent.putParcelableArrayListExtra("cartItems", new ArrayList<>(cartList));
        intent.putExtra("tax", tax);
        intent.putExtra("deliveryFee", deliveryFee);
        intent.putExtra("totalAmount", totalAmount);
        startActivity(intent);

    }
    private void checkUserInfo() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "Không xác định được người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            String name = snapshot.child("fullName").getValue(String.class);
            Address addressObj = snapshot.child("address").getValue(Address.class);

            boolean isAddressIncomplete = (addressObj == null
                    || addressObj.getStreet() == null || addressObj.getStreet().trim().isEmpty()
                    || addressObj.getCity() == null || addressObj.getCity().getCode() == -1
                    || addressObj.getDistrict() == null || addressObj.getDistrict().getCode() == -1
                    || addressObj.getWard() == null || addressObj.getWard().getCode() == -1);

            if (name == null || name.trim().isEmpty() || isAddressIncomplete) {
                new AlertDialog.Builder(this)
                        .setTitle("Thiếu thông tin cá nhân")
                        .setMessage("Bạn cần cập nhật họ tên và địa chỉ giao hàng trước khi thanh toán.")
                        .setPositiveButton("Cập nhật ngay", (dialog, which) -> {
                            Intent intent = new Intent(this, UserProfileActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                handleCheckOut(); // Đã đủ thông tin → tiếp tục
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi kiểm tra thông tin người dùng", Toast.LENGTH_SHORT).show();
        });
    }






}
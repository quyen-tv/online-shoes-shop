package com.prm392.onlineshoesshop.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.Api.CreateOrder;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.PaymentCartAdapter;
import com.prm392.onlineshoesshop.constant.AppInfo;
import com.prm392.onlineshoesshop.databinding.ActivityPaymentBinding;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.CartItem;
import com.prm392.onlineshoesshop.model.CreateOrderResult;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.model.TransactionItem;
import com.prm392.onlineshoesshop.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private TransactionRepository transactionRepository;
    private List<CartItem> cartItems;
    private double tax, deliveryFee, totalAmount;
    private String orderToken;
    private ManagementCart managementCart;
    private String appTransId;// Lưu appTransId từ ZaloPay
    private boolean hasCreatedTransaction = false; // Kiểm tra đã tạo Firebase transaction chưa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            appTransId = savedInstanceState.getString("appTransId");
            orderToken = savedInstanceState.getString("orderToken");
            hasCreatedTransaction = savedInstanceState.getBoolean("hasCreatedTransaction", false);
        }

        setupBtn();
        fetchAndBindUserInfo();

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        tax = intent.getDoubleExtra("tax", 0.0);
        deliveryFee = intent.getDoubleExtra("deliveryFee", 0.0);
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0);
        updateUIWithPaymentInfo();
        managementCart = new ManagementCart(this);

        transactionRepository = new TransactionRepository();

        // TODO: setup RecyclerView với PaymentCartAdapter
    }

    private void setupBtn() {
        binding.backBtn.setOnClickListener(v -> finish());
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
        binding.placeOrderBtn.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            if (paymentMethod.equals("Unknown")) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            if (paymentMethod.equals("ZaloPay")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    handleCheckOut();
                }
            }
            if (paymentMethod.equals("CashOnDelivery")) {
                    handleCheckOutWithCash();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private void handleCheckOut() {
        if (appTransId != null && orderToken != null) {
            // Đã tạo trước đó => chỉ gọi thanh toán
            handlePayOrder();
            return;
        }

        try {
            double usd = totalAmount;
            double rate = 25000;
            long vnd = Math.round(usd * rate);

            showLoading();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    CreateOrderResult result = new CreateOrder().createOrder(String.valueOf(vnd));

                    runOnUiThread(() -> {
                        try {
                            String code = result.rawData.getString("return_code");

                            if (code.equals("1")) {
                                orderToken = result.zpTransToken;
                                if (appTransId == null) {
                                    appTransId = result.appTransId;
                                }

                                // Lần đầu → tạo transaction
                                if (!hasCreatedTransaction) {
                                    hasCreatedTransaction = true;

                                    List<TransactionItem> simplifiedItems = new ArrayList<>();
                                    for (CartItem ci : cartItems) {
                                        ItemModel item = ci.getItem();
                                        simplifiedItems.add(new TransactionItem(
                                                item.getItemId(), item.getTitle(), ci.getQuantity(),
                                                item.getPrice(), ci.getSelectedSize(),
                                                item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : ""
                                        ));
                                    }

                                    transactionRepository.createPendingTransaction(
                                            appTransId,
                                            FirebaseAuth.getInstance().getUid(),
                                            usd, tax, deliveryFee,
                                            simplifiedItems,
                                            "ZaloPay"
                                    );
                                }

                                handlePayOrder(); // luôn gọi
                            } else {
                                hideLoading();
                                Toast.makeText(getApplicationContext(), "Tạo đơn ZaloPay thất bại", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            hideLoading();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(this::hideLoading);
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá trị không hợp lệ: " + totalAmount, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleCheckOutWithCash() {
        showLoading();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<TransactionItem> simplifiedItems = new ArrayList<>();
                for (CartItem ci : cartItems) {
                    ItemModel item = ci.getItem();
                    simplifiedItems.add(new TransactionItem(
                            item.getItemId(),
                            item.getTitle(),
                            ci.getQuantity(),
                            item.getPrice(),
                            ci.getSelectedSize(),
                            item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : ""
                    ));
                }

                // Tạo appTransId thủ công (hoặc có thể dùng timestamp, UUID,...)
                String appTransId = "CASH_" + UUID.randomUUID().toString();

                // Tạo giao dịch trạng thái PENDING
                transactionRepository.createPendingTransaction(
                        appTransId,
                        FirebaseAuth.getInstance().getUid(),
                        totalAmount,
                        tax,
                        deliveryFee,
                        simplifiedItems,
                        "CashOnDelivery"
                );
                transactionRepository.updateTransactionStatus(appTransId, Transaction.Status.PENDING, appTransId);

                managementCart.clearCart();

                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Đặt hàng thành công. Vui lòng thanh toán khi nhận hàng.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Lỗi khi tạo đơn hàng", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handlePayOrder() {
        ZaloPaySDK.getInstance().payOrder(this, orderToken, "demozpdk://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String zaloTransId, String transToken, String ignoredAppTransID) {
                runOnUiThread(() -> {
                    hideLoading();
                    updateStockInFirebase(cartItems);
                    transactionRepository.updateTransactionStatus(appTransId, Transaction.Status.SUCCESS, zaloTransId);
                    managementCart.clearCart();

                    Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                });
            }

            @Override
            public void onPaymentCanceled(String zpTransToken, String ignoredAppTransID) {
                runOnUiThread(() -> {
                    hideLoading();
                    showAlertDialog("User Cancel Payment", "This payment is cancelled");
                });
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String ignoredAppTransID) {
                runOnUiThread(() -> {
                    hideLoading();
                    showAlertDialog("Payment Fail", "ZaloPayErrorCode: " + zaloPayError.toString());
                });
            }
        });
    }

    private void updateStockInFirebase(List<CartItem> cartItems) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (CartItem cartItem : cartItems) {
            String itemId = cartItem.getItem().getItemId();
            String selectedSize = cartItem.getSelectedSize();
            int quantityToSubtract = cartItem.getQuantity();

            DatabaseReference stockRef = itemsRef.child(itemId).child("stockEntries");

            stockRef.get().addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItemModel.StockEntry entry = snapshot.getValue(ItemModel.StockEntry.class);
                    if (entry == null || entry.getSize() == null) continue;

                    if (entry.getSize().equals(selectedSize)) {
                        int newQty = Math.max(entry.getQuantity() - quantityToSubtract, 0);
                        snapshot.getRef().child("quantity").setValue(newQty);
                        break;
                    }
                }
            }).addOnFailureListener(e -> Log.e("StockUpdate", "Lỗi cập nhật tồn kho: " + e.getMessage()));
        }
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading() {
        binding.progressBarPayment.setVisibility(View.VISIBLE);
        binding.placeOrderBtn.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBarPayment.setVisibility(View.GONE);
        binding.placeOrderBtn.setEnabled(true);
    }

    private void updateUIWithPaymentInfo() {
        if (cartItems == null || cartItems.isEmpty()) {
            binding.scrollView2.setVisibility(View.GONE);
            Toast.makeText(this, "Không có sản phẩm nào để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup RecyclerView
        PaymentCartAdapter adapter = new PaymentCartAdapter(this, cartItems);
        binding.viewCart.setLayoutManager(new LinearLayoutManager(this));
        binding.viewCart.setAdapter(adapter);

        // Cập nhật các thông tin tính tiền
        binding.totalFeeTxt.setText("$" + String.format("%.2f", getSubtotal()));
        binding.taxTxt.setText("$" + String.format("%.2f", tax));
        binding.deliveryTxt.setText("$" + String.format("%.2f", deliveryFee));
        binding.totalTxt.setText("$" + String.format("%.2f", totalAmount));
    }
    private double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getItem().getPrice() * item.getQuantity();
        }
        return subtotal;
    }
    private String getSelectedPaymentMethod() {
        int selectedId = binding.paymentMethodGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.zaloPayRadio) {
            return "ZaloPay";
        } else if (selectedId == R.id.cashRadio) {
            return "CashOnDelivery";
        } else {
            return "Unknown"; // hoặc return null nếu muốn kiểm tra lỗi
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // rất quan trọng nếu bạn muốn đọc intent sau đó

        Log.d("ZaloPayDebug", "onNewIntent called: " + intent.getDataString());

        // Đảm bảo SDK xử lý callback
        ZaloPaySDK.getInstance().onResult(intent);
    }

    private void fetchAndBindUserInfo() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                Address address = snapshot.child("address").getValue(Address.class);

                // Gộp địa chỉ
                StringBuilder fullAddress = new StringBuilder();
                if (address != null) {
                    if (address.getStreet() != null && !address.getStreet().isEmpty())
                        fullAddress.append(address.getStreet()).append(", ");
                    if (address.getWard() != null && address.getWard().getName() != null)
                        fullAddress.append(address.getWard().getName()).append(", ");
                    if (address.getDistrict() != null && address.getDistrict().getName() != null)
                        fullAddress.append(address.getDistrict().getName()).append(", ");
                    if (address.getCity() != null && address.getCity().getName() != null)
                        fullAddress.append(address.getCity().getName());
                }

                binding.userNameTxt.setText(name != null ? name : "...");
                binding.userEmailTxt.setText(email != null ? email : "...");
                binding.userAddressTxt.setText(fullAddress.length() > 0 ? fullAddress.toString() : "...");
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("UserInfo", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("appTransId", appTransId);
        outState.putString("orderToken", orderToken);
        outState.putBoolean("hasCreatedTransaction", hasCreatedTransaction);

    }

}

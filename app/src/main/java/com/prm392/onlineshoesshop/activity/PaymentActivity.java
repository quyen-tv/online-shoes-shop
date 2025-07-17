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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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
import com.prm392.onlineshoesshop.utils.UiUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private String appTransId;
    private boolean hasCreatedTransaction = false;

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

        transactionRepository = new TransactionRepository();
        setupBtn();
        fetchAndBindUserInfo();
        managementCart = new ManagementCart(this);

        Intent intent = getIntent();
        String passedAppTransId = intent.getStringExtra("appTransId");

        if (passedAppTransId != null) {
            // Retry payment
            appTransId = passedAppTransId;
            hasCreatedTransaction = true;
            fetchTransactionDataAndBuildCart(appTransId);

            // 👉 Set radio ZaloPay và disable chọn lại
            binding.zaloPayRadio.setChecked(true);
            binding.zaloPayRadio.setEnabled(false);
            binding.cashRadio.setEnabled(false);
        } else {
            // Đặt hàng mới
            cartItems = intent.getParcelableArrayListExtra("cartItems");
            tax = intent.getDoubleExtra("tax", 0.0);
            deliveryFee = intent.getDoubleExtra("deliveryFee", 0.0);
            totalAmount = intent.getDoubleExtra("totalAmount", 0.0);

            updateUIWithPaymentInfo();
        }

    }
    private void fetchTransactionDataAndBuildCart(String appTransId) {
        DatabaseReference transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(appTransId);

        transRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(this, "Không tìm thấy giao dịch để thanh toán lại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Lấy các trường cần thiết
            tax = snapshot.child("tax").getValue(Double.class);
            deliveryFee = snapshot.child("deliveryFee").getValue(Double.class);
            totalAmount = snapshot.child("totalAmount").getValue(Double.class);

            List<TransactionItem> transactionItems = new ArrayList<>();
            for (DataSnapshot itemSnap : snapshot.child("items").getChildren()) {
                TransactionItem item = itemSnap.getValue(TransactionItem.class);
                if (item != null) {
                    transactionItems.add(item);
                }
            }

            cartItems = new ArrayList<>();
            for (TransactionItem ti : transactionItems) {
                ItemModel item = new ItemModel();
                item.setItemId(ti.getItemId());
                item.setTitle(ti.getName());
                item.setPrice(ti.getPrice());
                List<String> pics = new ArrayList<>();
                pics.add(ti.getPicUrl());
                item.setPicUrl(pics);

                cartItems.add(new CartItem(item, ti.getSize(), ti.getQuantity()));
            }

            updateUIWithPaymentInfo(); // render lại
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải lại đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        });
    }


    private void setupBtn() {
        binding.backBtn.setOnClickListener(v -> finish());
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
        binding.placeOrderBtn.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            if (paymentMethod.equals("Unknown")) {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Vui lòng chọn phương thức thanh toán",
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.warning_orange));
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

            long vnd = Math.round(totalAmount);

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
                                                item.getPicUrl() != null && !item.getPicUrl().isEmpty()
                                                        ? item.getPicUrl().get(0)
                                                        : ""));
                                    }

                                    transactionRepository.createPendingTransaction(
                                            appTransId,
                                            FirebaseAuth.getInstance().getUid(),
                                            vnd, tax, deliveryFee,
                                            simplifiedItems,
                                            "ZaloPay");
                                    decreaseStock(cartItems);

                                }

                                handlePayOrder(); // luôn gọi
                            } else {
                                hideLoading();
                                UiUtils.showSnackbarWithBackground(
                                        binding.getRoot(),
                                        "Tạo đơn ZaloPay thất bại",
                                        Snackbar.LENGTH_LONG,
                                        getResources().getColor(R.color.error_red));
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
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Giá trị không hợp lệ: " + totalAmount,
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.warning_orange));
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
                            item.getPicUrl() != null && !item.getPicUrl().isEmpty() ? item.getPicUrl().get(0) : ""));
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
                transactionRepository.updatePaymentStatus(appTransId, Transaction.PaymentStatus.PENDING, appTransId);
                transactionRepository.updateOrderStatus(appTransId, Transaction.OrderStatus.WAITING_CONFIRMATION);

                managementCart.clearCart();
                decreaseStock(cartItems);
                increaseSold(cartItems);

                runOnUiThread(() -> {
                    hideLoading();
                    Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    hideLoading();
                    UiUtils.showSnackbarWithBackground(
                            binding.getRoot(),
                            "Lỗi khi tạo đơn hàng",
                            Snackbar.LENGTH_LONG,
                            getResources().getColor(R.color.error_red));
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
                    increaseSold(cartItems);
                    transactionRepository.updatePaymentStatus(appTransId, Transaction.PaymentStatus.SUCCESS, zaloTransId);
                    transactionRepository.updateOrderStatus(appTransId, Transaction.OrderStatus.WAITING_CONFIRMATION);

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

    private void decreaseStock(List<CartItem> cartItems) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (CartItem cartItem : cartItems) {
            String itemId = cartItem.getItem().getItemId();
            String selectedSize = cartItem.getSelectedSize();
            int quantityToSubtract = cartItem.getQuantity();

            DatabaseReference stockRef = itemsRef.child(itemId).child("stockEntries");

            stockRef.get().addOnSuccessListener(dataSnapshot -> {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItemModel.StockEntry entry = snapshot.getValue(ItemModel.StockEntry.class);
                    if (entry != null && selectedSize.equals(entry.getSize())) {
                        int newQty = Math.max(entry.getQuantity() - quantityToSubtract, 0);
                        snapshot.getRef().child("quantity").setValue(newQty);
                        break;
                    }
                }
            }).addOnFailureListener(e -> Log.e("StockUpdate", "Lỗi giảm tồn kho: " + e.getMessage()));
        }
    }

    private void increaseSold(List<CartItem> cartItems) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("Items");

        for (CartItem cartItem : cartItems) {
            String itemId = cartItem.getItem().getItemId();
            int quantityToAdd = cartItem.getQuantity();

            DatabaseReference itemRef = itemsRef.child(itemId);

            itemRef.child("sold").get().addOnSuccessListener(snap -> {
                Long currentSold = snap.getValue(Long.class);
                long newSold = (currentSold != null ? currentSold : 0) + quantityToAdd;
                itemRef.child("sold").setValue(newSold);
            }).addOnFailureListener(e -> Log.e("StockUpdate", "Lỗi tăng sold: " + e.getMessage()));
        }
    }


    private void showAlertDialog(String title, String message) {
        new MaterialAlertDialogBuilder(this)
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
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Không có sản phẩm nào để thanh toán",
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.warning_orange));
            return;
        }

        // Setup RecyclerView
        PaymentCartAdapter adapter = new PaymentCartAdapter(this, cartItems);
        binding.viewCart.setLayoutManager(new LinearLayoutManager(this));
        binding.viewCart.setAdapter(adapter);

        // Cập nhật các thông tin tính tiền
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        binding.totalFeeTxt.setText("₫" + format.format(getSubtotal()));
        binding.taxTxt.setText("₫" + format.format(tax));
        binding.deliveryTxt.setText("₫" + format.format(deliveryFee));
        binding.totalTxt.setText("₫" + format.format(totalAmount));

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
        if (uid == null)
            return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phoneNumber").getValue(String.class);
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
                binding.userPhoneTxt.setText(phone != null ? phone : "...");
                binding.userAddressTxt.setText(fullAddress.length() > 0 ? fullAddress.toString() : "...");
            } else {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Không tìm thấy thông tin người dùng",
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.error_red));
            }
        }).addOnFailureListener(e -> {
            Log.e("UserInfo", "Lỗi khi lấy thông tin người dùng: " + e.getMessage());
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Lỗi khi tải thông tin người dùng",
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.error_red));
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

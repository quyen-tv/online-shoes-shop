package com.prm392.onlineshoesshop.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prm392.onlineshoesshop.Api.CreateOrder;
import com.prm392.onlineshoesshop.adapter.CartAdapter;
import com.prm392.onlineshoesshop.constant.AppInfo;
import com.prm392.onlineshoesshop.databinding.ActivityCartBinding;
import com.prm392.onlineshoesshop.helper.ChangeNumberItemsListener;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.CreateOrderResult;
import com.prm392.onlineshoesshop.model.Transaction;
import com.prm392.onlineshoesshop.repository.TransactionRepository;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private ManagementCart managementCart;
    private double tax;
    private String orderToken;
    private TransactionRepository transactionRepository;

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
        transactionRepository = new TransactionRepository();

        setVariable();
        initCartList();
        calculateCart();
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());
        // ZaloPay SDK Init
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
        // handle CreateOrder

        binding.btnCheckOut.setOnClickListener(v -> {
            handleCheckOut();

        });

    }

    private void initCartList() {
        binding.viewCart.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        binding.viewCart.setAdapter(new CartAdapter(managementCart.getItemList(), this, new ChangeNumberItemsListener() {
            @Override
            public void onChanged() {
                calculateCart();
            }
        }));

        if (managementCart.getItemList().isEmpty()) {
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private void handleCheckOut() {
        String usdAmountStr = binding.totalTxt.getText().toString().replace("$", "").trim();

        try {
            double usd = Double.parseDouble(usdAmountStr);
            double rate = 25000;
            long vnd = Math.round(usd * rate);

            Log.d("ZaloPayDebug", "USD: " + usd + " | VND: " + vnd);

            showLoading();

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    CreateOrderResult result = new CreateOrder().createOrder(String.valueOf(vnd));
                    Log.d("ZaloPayDebug", "CreateOrder response: " + result.rawData.toString());

                    runOnUiThread(() -> {
                        try {
                            String code = result.rawData.getString("return_code");
                            Log.d("ZaloPayDebug", "Return code: " + code);

                            if (code.equals("1")) {
                                // Gán token để xử lý tiếp
                                orderToken = result.zpTransToken;

                                // Tạo giao dịch trạng thái PENDING
                                transactionRepository.createPendingTransaction(
                                        result.appTransId,
                                        "demo_user_id",  // sau này dùng FirebaseAuth.getInstance().getUid()
                                        usd,
                                        tax,
                                        10.0,
                                        managementCart.getItemList(),
                                        "ZaloPay"
                                );

                                // Thanh toán
                                handlePayOrder();
                            } else {
                                hideLoading();
                                Toast.makeText(getApplicationContext(), "Tạo đơn hàng thất bại", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            hideLoading();
                            Log.e("ZaloPayDebug", "Lỗi xử lý JSON: " + e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(this::hideLoading);
                    Log.e("ZaloPayDebug", "Lỗi gọi API tạo đơn: " + e.getMessage());
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá trị không hợp lệ: " + usdAmountStr, Toast.LENGTH_SHORT).show();
            Log.e("ZaloPayDebug", "Lỗi parse USD: " + e.getMessage());
        }
    }



    private void handlePayOrder() {
        String token = orderToken;

        ZaloPaySDK.getInstance().payOrder(this, token, "demozpdk://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                runOnUiThread(() -> {
                    hideLoading();

                    // ✅ Cập nhật trạng thái thành công
                    transactionRepository.updateTransactionStatus(appTransID, Transaction.Status.SUCCESS, transactionId);

                    // ✅ Xoá giỏ hàng
                    managementCart.clearCart();

                    // ✅ Refresh lại UI (ẩn cart, hiện empty)
                    initCartList();
                    calculateCart();
                    
                });
            }


            @Override
            public void onPaymentCanceled(String zpTransToken, String appTransID) {
                runOnUiThread(() -> {
                    hideLoading();
                    // ❌ Người dùng huỷ → Failed (hoặc dùng CANCELED nếu bạn định nghĩa thêm enum)
                    transactionRepository.updateTransactionStatus(appTransID, Transaction.Status.FAILED, null);
                    showAlertDialog("User Cancel Payment",
                            String.format("zpTransToken: %s", zpTransToken));
                });
            }

            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                runOnUiThread(() -> {
                    hideLoading();
                    // ❌ Lỗi kỹ thuật → Failed
                    transactionRepository.updateTransactionStatus(appTransID, Transaction.Status.FAILED, null);
                    showAlertDialog("Payment Fail",
                            String.format("ZaloPayErrorCode: %s\nTransToken: %s", zaloPayError.toString(), zpTransToken));
                });
            }
        });
    }

    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // rất quan trọng nếu bạn muốn đọc intent sau đó

        Log.d("ZaloPayDebug", "onNewIntent called: " + intent.getDataString());

        // Đảm bảo SDK xử lý callback
        ZaloPaySDK.getInstance().onResult(intent);
    }
    private void showLoading() {
        binding.progressBarPayment.setVisibility(View.VISIBLE);
        binding.btnCheckOut.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBarPayment.setVisibility(View.GONE);
        binding.btnCheckOut.setEnabled(true);
    }


}
package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.TransactionAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityTransactionHistoryBinding;
import com.prm392.onlineshoesshop.model.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private ActivityTransactionHistoryBinding binding;
    private TransactionAdapter transactionAdapter;

    // danh sách gốc
    private final List<Transaction> allTransactions   = new ArrayList<>();
    // 5 “view”-list đã lọc sẵn
    private final List<Transaction> waitingList       = new ArrayList<>();
    private final List<Transaction> pickupList        = new ArrayList<>();
    private final List<Transaction> shippingList      = new ArrayList<>();
    private final List<Transaction> deliveredList     = new ArrayList<>();
    private final List<Transaction> cancelledList     = new ArrayList<>();

    // trạng thái nút hiện hành (mặc định “Chờ xác nhận”)
    private enum Filter {WAITING, PICKUP, SHIPPING, DELIVERED, CANCELLED}
    private Filter currentFilter = Filter.WAITING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupButtons();
        loadTransactions();
        initBottomNavigation();
    }

    /* ------------------------------------------------------------------ */
    /* RecyclerView                                                       */
    /* ------------------------------------------------------------------ */
    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(new ArrayList<>(), this);
        binding.transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionRecyclerView.setAdapter(transactionAdapter);
    }

    /* ------------------------------------------------------------------ */
    /* Firebase fetch + chuẩn bị 5 list con                               */
    /* ------------------------------------------------------------------ */
    private void loadTransactions() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Log.w("TransactionHistory", "Chưa đăng nhập");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("transactions");

        ref.orderByChild("userId").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        // reset
                        allTransactions.clear();
                        waitingList.clear();
                        pickupList.clear();
                        shippingList.clear();
                        deliveredList.clear();
                        cancelledList.clear();

                        for (DataSnapshot child : snap.getChildren()) {
                            Transaction t = child.getValue(Transaction.class);
                            if (t == null) continue;

                            allTransactions.add(t);

                            switch (t.getOrderStatus()) {
                                case WAITING_CONFIRMATION:
                                    waitingList.add(t);
                                    break;
                                case WAITING_FOR_PICKUP:
                                    pickupList.add(t);
                                    break;
                                case DELIVERING:
                                    shippingList.add(t);
                                    break;
                                case DELIVERED:
                                    deliveredList.add(t);
                                    break;
                                case CANCELLED:
                                    cancelledList.add(t);
                                    break;
                            }

                        }

                        // sort mới-nhất → cũ-nhất cho mọi list
                        sortDesc(waitingList);  sortDesc(pickupList);
                        sortDesc(shippingList); sortDesc(deliveredList);
                        sortDesc(cancelledList);

                        updateBadges();
                        showCurrentList();   // refresh màn hình
                    }

                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        Log.e("TransactionHistory", "Firebase error: " + e.getMessage());
                    }
                });
    }

    private void sortDesc(List<Transaction> list) {
        Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
    }

    /* ------------------------------------------------------------------ */
    /* Badge = số + hiển/ẩn                                               */
    /* ------------------------------------------------------------------ */
    private void updateBadges() {
        setBadge(binding.badgeWaiting,          waitingList.size());
        setBadge(binding.badgePickup, pickupList.size());
        setBadge(binding.badgeShipping,         shippingList.size());
        setBadge(binding.badgeDelivered,        deliveredList.size());
        setBadge(binding.badgeCancelled,        cancelledList.size());
    }

    private void setBadge(TextView badgeView, int count) {
        badgeView.setText(String.valueOf(count));
        badgeView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    /* ------------------------------------------------------------------ */
    /* Hiển thị list theo currentFilter                                   */
    /* ------------------------------------------------------------------ */
    private void showCurrentList() {
        List<Transaction> toShow = new ArrayList<>();

        switch (currentFilter) {
            case WAITING:
                toShow = waitingList;
                break;
            case PICKUP:
                toShow = pickupList;
                break;
            case SHIPPING:
                toShow = shippingList;
                break;
            case DELIVERED:
                toShow = deliveredList;
                break;
            case CANCELLED:
                toShow = cancelledList;
                break;
        }

        transactionAdapter.setData(toShow);
        binding.emptyTxt.setVisibility(toShow.isEmpty() ? View.VISIBLE : View.GONE);
    }


    /* ------------------------------------------------------------------ */
    /* Button click events                                                */
    /* ------------------------------------------------------------------ */
    private void setupButtons() {

        binding.btnWaiting.setOnClickListener(v -> {
            currentFilter = Filter.WAITING;
            showCurrentList();
        });

        binding.btnPickup.setOnClickListener(v -> {
            currentFilter = Filter.PICKUP;
            showCurrentList();
        });

        binding.btnShipping.setOnClickListener(v -> {
            currentFilter = Filter.SHIPPING;
            showCurrentList();
        });

        binding.btnDelivered.setOnClickListener(v -> {
            currentFilter = Filter.DELIVERED;
            showCurrentList();
        });

        binding.btnCancelled.setOnClickListener(v -> {
            currentFilter = Filter.CANCELLED;
            showCurrentList();
        });

        // hiển thị mặc định
        showCurrentList();
    }

    /* ------------------------------------------------------------------ */
    /* BottomNavigation                                                   */
    /* ------------------------------------------------------------------ */
    private void initBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_my_order);

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_notification) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_explorer) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this, UserSettingsActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_favorite) {
                startActivity(new Intent(this, FavoriteActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}

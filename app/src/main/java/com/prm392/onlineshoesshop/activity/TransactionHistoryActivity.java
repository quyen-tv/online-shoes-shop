package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
    private final List<Transaction> transactionList = new ArrayList<>();
    private TransactionAdapter transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        loadTransactions();
        binding.backBtn.setOnClickListener(v -> finish());
        initBottomNavigation();
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(transactionList, this);
        binding.transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionRecyclerView.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Log.w("TransactionActivity", "User chưa đăng nhập. Không thể load transactions.");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("transactions");
        ref.orderByChild("userId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactionList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Transaction transaction = child.getValue(Transaction.class);
                            if (transaction != null) {
                                transactionList.add(transaction);
                            }
                        }

                        // Sắp xếp theo thời gian giảm dần
                        Collections.sort(transactionList, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));

                        transactionAdapter.notifyDataSetChanged();
                        binding.emptyTxt.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);

                        Log.d("TransactionActivity", "Loaded " + transactionList.size() + " transactions.");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TransactionActivity", "Lỗi khi load transactions: " + error.getMessage());
                    }
                });
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_my_order);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_explorer) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this, UserSettingsActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_favorite) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            }
            return false;
        });
    }
}

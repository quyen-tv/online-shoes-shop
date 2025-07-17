package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.helper.TinyDB;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.adapter.AllItemAdapter;
import java.util.*;

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

        // Hiển thị danh sách sản phẩm gợi ý
        setupRecommendedProducts();
    }

    private void setupRecommendedProducts() {
        // Lấy lịch sử sản phẩm đã xem
        TinyDB tinyDB = new TinyDB(this);
        ArrayList<ItemModel> viewed = tinyDB.getListObject("ViewHistoryList");
        if (viewed == null)
            viewed = new ArrayList<>();

        List<ItemModel> displayList = new ArrayList<>(viewed);
        Collections.reverse(displayList);
        AllItemAdapter adapter = new AllItemAdapter(displayList);
        RecyclerView rv = findViewById(R.id.rvSimilar);
        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        binding.rvRecommended.setLayoutManager(layoutManager);
        binding.rvRecommended.setAdapter(adapter);
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        binding.rvRecommended.addItemDecoration(new SpaceItemDecoration(spacing, spanCount));
    }
}

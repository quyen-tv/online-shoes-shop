package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.AllItemAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityFavoriteBinding;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.helper.TinyDB;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.List;

public class ViewHistoryActivity extends AppCompatActivity implements AllItemAdapter.OnChangeListener {
    private ActivityFavoriteBinding binding;
    private AllItemAdapter adapter;
    private static final int GRID_SPAN_COUNT = 2;
    private List<ItemModel> historyItems = new ArrayList<>();
    private ItemViewModel itemViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvFavorite.setText(getString(R.string.settings_view_history));
        binding.cartIconContainer.setVisibility(View.GONE);
        binding.ivChat.setVisibility(View.GONE);
        binding.navigation.setVisibility(View.GONE);
        binding.btnBack.setVisibility(View.VISIBLE);
        binding.filterCriteriaLayout.setVisibility(View.GONE);
        binding.sortLayout.setVisibility(View.GONE);
        binding.textInputLayoutSearch.setVisibility(View.GONE);
        binding.ivSearchIcon.setVisibility(View.GONE);
        binding.progressBarItems.setVisibility(View.GONE);

        // Khởi tạo ViewModel để lấy trạng thái yêu thích
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel.class);

        TinyDB tinyDB = new TinyDB(this);
        historyItems = tinyDB.getListObject("ViewHistoryList");
        if (historyItems == null)
            historyItems = new ArrayList<>();
        setupRecyclerView(historyItems);

        // Lắng nghe trạng thái yêu thích để cập nhật UI
        itemViewModel.currentUserData.observe(this, user -> {
            if (user != null && user.getFavoriteItems() != null) {
                List<String> favoriteIds = new ArrayList<>();
                for (java.util.Map.Entry<String, Boolean> entry : user.getFavoriteItems().entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        favoriteIds.add(entry.getKey());
                    }
                }
                if (adapter != null) {
                    adapter.setFavoriteIds(favoriteIds);
                }
            }
        });

        // Hiển thị thông báo nếu không có sản phẩm nào
        if (historyItems.isEmpty()) {
            binding.viewFavorites.setVisibility(View.GONE);
            binding.tvNoResult.setVisibility(View.VISIBLE);
            binding.tvNoResult.setText("Bạn chưa xem sản phẩm nào!");
        } else {
            binding.viewFavorites.setVisibility(View.VISIBLE);
            binding.tvNoResult.setVisibility(View.GONE);
        }

        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(ViewHistoryActivity.this, CartActivity.class);
            startActivity(intent);
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView(List<ItemModel> items) {
        adapter = new AllItemAdapter(items);
        adapter.setOnChangeListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
        binding.viewFavorites.setLayoutManager(layoutManager);
        binding.viewFavorites.setAdapter(adapter);
    }

    @Override
    public void onToggleFavorite(String itemId) {
        itemViewModel.toggleFavorite(itemId);
    }

    @Override
    public void onClick(ItemModel item) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }
}
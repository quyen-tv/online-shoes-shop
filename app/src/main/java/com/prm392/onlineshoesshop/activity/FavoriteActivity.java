package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.FavoriteAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityFavoriteBinding;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnItemClickListener {

    private ActivityFavoriteBinding binding;
    private FavoriteAdapter adapter;
    private ItemViewModel itemViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewModel();
        observeUserData();
        initBottomNavigation();
    }

    private void setupViewModel() {
        UserRepository repository = new UserRepository(); // hoặc inject từ đâu đó
        itemViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new ItemViewModel(repository);
            }
        }).get(ItemViewModel.class);

        itemViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<ItemModel> items) {
        adapter = new FavoriteAdapter(this, items, itemViewModel, this);
        binding.recyclerFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerFavorites.setAdapter(adapter);
    }


    private void observeUserData() {
        binding.progressBarFavorite.setVisibility(View.VISIBLE);

        itemViewModel.currentUserData.observe(this, user -> {
            if (user != null && user.getFavoriteItems() != null) {
                itemViewModel.fetchFavoriteItems(favoriteItems -> {
                    setupRecyclerView(favoriteItems);
                    binding.progressBarFavorite.setVisibility(View.GONE);
                });

            }
        });
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_favorite);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));

                return false;
            }
            if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this, UserSettingsActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.navigation_explorer) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onItemClicked(ItemModel item) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        itemViewModel.forceRefreshUserData();
    }
}

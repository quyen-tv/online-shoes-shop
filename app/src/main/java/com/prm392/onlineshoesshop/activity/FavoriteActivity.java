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

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.FavoriteAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityFavoriteBinding;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.model.FilterState;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ChipStyleUtils;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnChangeListener {

    private ActivityFavoriteBinding binding;
    private FavoriteAdapter adapter;
    private ItemViewModel itemViewModel;
    private static final int GRID_SPAN_COUNT = 2;
    private int selectedPriceIndex = 0;
    private String[] priceRanges;
    private FilterState filterState = new FilterState();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        priceRanges = new String[] {
                getString(R.string.price_range_all),
                getString(R.string.price_range_under_10m),
                getString(R.string.price_range_10_16m),
                getString(R.string.price_range_16_22m),
                getString(R.string.price_range_over_22m)
        };

        setupViewModel();
        observeUserData();
        initFilterChips();
        initBottomNavigation();
    }

    private void setupViewModel() {
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel.class);

        itemViewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<ItemModel> items) {
        adapter = new FavoriteAdapter(items);
        adapter.setOnChangeListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
        binding.viewFavorites.setLayoutManager(layoutManager);
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        binding.viewFavorites.addItemDecoration(new SpaceItemDecoration(spacing, GRID_SPAN_COUNT));
        binding.viewFavorites.setAdapter(adapter);
    }

    private void observeUserData() {
        binding.progressBarItems.setVisibility(View.VISIBLE);
        itemViewModel.currentUserData.observe(this, user -> {
            if (user != null && user.getFavoriteItems() != null) {
                List<String> favoriteIds = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : user.getFavoriteItems().entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        favoriteIds.add(entry.getKey());
                    }
                }
                itemViewModel.fetchFavoriteItems(favoriteItems -> {
                    setupRecyclerView(favoriteItems);
                    if (adapter != null) {
                        adapter.setFavoriteIds(favoriteIds);
                    }
                    binding.progressBarItems.setVisibility(View.GONE);
                });
            }
        });
    }

    private void initFilterChips() {
        binding.chipInStock.setOnClickListener(v -> {
            boolean selected = !binding.chipInStock.isChecked();
            updateChipAppearance(binding.chipInStock, selected);
        });

        binding.chipPriceRange.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(getString(R.string.title_select_price_range));
            builder.setSingleChoiceItems(priceRanges, selectedPriceIndex, (dialog, which) -> {
                selectedPriceIndex = which;
                if (selectedPriceIndex == 0) { // Tất cả
                    binding.chipPriceRange.setText(getString(R.string.chip_price_range_default));
                    ChipStyleUtils.applyStyle(this, binding.chipPriceRange, false);
                } else {
                    binding.chipPriceRange.setText(priceRanges[which]);
                    ChipStyleUtils.applyStyle(this, binding.chipPriceRange, true);
                }
                dialog.dismiss();
            });
            builder.show();
        });

        binding.chipSortPriceLow.setOnClickListener(v -> {
            handleSortChipClick(FilterState.SortType.PRICE_LOW);
        });

        binding.chipSortPriceHigh.setOnClickListener(v -> {
            handleSortChipClick(FilterState.SortType.PRICE_HIGH);
        });

        binding.chipSortPopular.setOnClickListener(v -> {
            handleSortChipClick(FilterState.SortType.POPULAR);
        });
    }

    private void handleSortChipClick(FilterState.SortType sortType) {
        if (filterState.getSortType() == sortType) {
            filterState = filterState.setSortType(FilterState.SortType.NONE);
        } else {
            filterState = filterState.setSortType(sortType);
        }
        updateSortChipsAppearance();
    }

    private void updateSortChipsAppearance() {
        updateChipAppearance(binding.chipSortPriceLow, filterState.getSortType() == FilterState.SortType.PRICE_LOW);
        updateChipAppearance(binding.chipSortPriceHigh, filterState.getSortType() == FilterState.SortType.PRICE_HIGH);
        updateChipAppearance(binding.chipSortPopular, filterState.getSortType() == FilterState.SortType.POPULAR);
    }

    private void updateChipAppearance(@NonNull Chip chip, boolean isSelected) {
        ChipStyleUtils.applyStyle(this, chip, isSelected);
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
}

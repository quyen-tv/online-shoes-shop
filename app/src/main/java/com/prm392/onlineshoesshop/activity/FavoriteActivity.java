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
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.FilterState;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ChipStyleUtils;
import com.prm392.onlineshoesshop.utils.PriceRangeDialog;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnChangeListener {

    private ActivityFavoriteBinding binding;
    private FavoriteAdapter adapter;
    private ItemViewModel itemViewModel;
    private AuthViewModel authViewModel;
    private static final int GRID_SPAN_COUNT = 2;
    private int selectedPriceIndex = 0;
    private String[] priceRanges;
    private FilterState filterState = new FilterState();
    private boolean isDecorationAdded = false;
    private List<ItemModel> allFavoriteItems = new ArrayList<>();
    private String currentSearchQuery = "";
    private PriceRangeDialog priceRangeDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Sử dụng resource string cho các mức giá
        priceRanges = new String[] {
                getString(R.string.price_range_all), // vị trí index 0
                getString(R.string.chip_price_range_under_50), // UNDER_50
                getString(R.string.chip_price_range_50_100), // FIFTY_TO_100
                getString(R.string.chip_price_range_100_200), // HUNDRED_TO_200
                getString(R.string.chip_price_range_over_200) // OVER_200
        };

        // Khởi tạo PriceRangeDialog giống AllItemsActivity
        priceRangeDialog = new PriceRangeDialog(this, (minPrice, maxPrice, rangeType) -> {
            filterState = filterState.setPriceRange(minPrice, maxPrice, rangeType);
            updateAllChipsAppearance();
            applyFilters();
        });

        binding.btnBack.setVisibility(View.GONE);

        setupViewModel();
        observeUserData();
        initFilterChips();
        initBottomNavigation();

        // Sự kiện click nút search: ẩn title, hiện ô search, focus, ẩn icon search
        binding.ivSearchIcon.setOnClickListener(v -> {
            binding.tvFavorite.setVisibility(View.GONE);
            binding.textInputLayoutSearch.setVisibility(View.VISIBLE);
            binding.etSearch.requestFocus();
            binding.ivSearchIcon.setVisibility(View.GONE);
            // Hiện bàn phím
            binding.etSearch.post(() -> {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            });
        });

        // Khi nhập search realtime
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // Sự kiện click icon end (ic_close) trong ô search: hiện lại title, ẩn ô
        // search, hiện lại icon search, xóa text search
        binding.textInputLayoutSearch.setEndIconOnClickListener(v -> {
            binding.textInputLayoutSearch.setVisibility(View.GONE);
            binding.tvFavorite.setVisibility(View.VISIBLE);
            binding.ivSearchIcon.setVisibility(View.VISIBLE);
            binding.etSearch.setText("");
            currentSearchQuery = "";
            applyFilters();
            // Ẩn bàn phím
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
            }
        });

        // Sự kiện click nút cart
        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(FavoriteActivity.this, CartActivity.class);
            startActivity(intent);
        });

        binding.ivChat.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
    }

    private void setupViewModel() {
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel.class);
        AuthViewModelFactory authViewModelFactory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(
                this,
                authViewModelFactory)
                .get(AuthViewModel.class);
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
        if (!isDecorationAdded) {
            binding.viewFavorites.addItemDecoration(new SpaceItemDecoration(spacing, GRID_SPAN_COUNT));
            isDecorationAdded = true;
        }
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
                    allFavoriteItems = favoriteItems;
                    applyFilters(); // áp dụng filter (viết hàm ở bước sau)

                    if (adapter == null) {
                        setupRecyclerView(favoriteItems); // chỉ tạo adapter 1 lần
                    }
                    adapter.setFavoriteIds(favoriteIds);
                    allFavoriteItems = favoriteItems;
                    applyFilters(); // không tạo lại adapter nữa

                    binding.progressBarItems.setVisibility(View.GONE);
                });
            }
        });
    }

    private void initFilterChips() {
        binding.chipInStock.setOnClickListener(v -> {
            filterState = filterState.toggleInStock();
            updateAllChipsAppearance();
            applyFilters();
        });

        binding.chipPriceRange.setOnClickListener(v -> {
            priceRangeDialog.show(filterState.getPriceRangeType(), filterState.getMinPrice(),
                    filterState.getMaxPrice());
        });

        binding.chipSortPriceLow.setOnClickListener(v -> {
            handleSortChipClick(FilterState.SortType.PRICE_LOW);
        });

        binding.chipSortPriceHigh.setOnClickListener(v -> {
            handleSortChipClick(FilterState.SortType.PRICE_HIGH);
        });

        binding.chipSortPopular.setOnClickListener(v -> handleSortChipClick(FilterState.SortType.POPULAR));
        binding.chipSortPopular.setVisibility(View.VISIBLE);

    }

    private void handleSortChipClick(FilterState.SortType sortType) {
        if (filterState.getSortType() == sortType) {
            filterState = filterState.setSortType(FilterState.SortType.NONE);
        } else {
            filterState = filterState.setSortType(sortType);
        }
        updateAllChipsAppearance();
        applyFilters();
    }

    private void updateSortChipsAppearance() {
        updateChipAppearance(binding.chipSortPriceLow, filterState.getSortType() == FilterState.SortType.PRICE_LOW);
        updateChipAppearance(binding.chipSortPriceHigh, filterState.getSortType() == FilterState.SortType.PRICE_HIGH);
        updateChipAppearance(binding.chipSortPopular, filterState.getSortType() == FilterState.SortType.POPULAR);
    }

    private void updateAllChipsAppearance() {
        updateChipAppearance(binding.chipInStock, filterState.isInStockSelected());
        // Cập nhật chipPriceRange: chỉ các mốc khác NONE mới tím
        updatePriceRangeChipText(filterState.getMinPrice(), filterState.getMaxPrice(), filterState.getPriceRangeType());
        updateSortChipsAppearance();
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
            if (item.getItemId() == R.id.navigation_notification) {
                startActivity(new Intent(this, CartActivity.class));

                return false;
            }
            if (item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this, UserSettingsActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.navigation_explorer) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.navigation_my_order) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        authViewModel.reloadCurrentUser();
        updateCartBadge();
    }

    private void updateCartBadge() {
        ManagementCart managementCart = new ManagementCart(this);
        int count = managementCart.getCartItems().size();
        if (count > 0) {
            binding.tvCartBadge.setText(String.valueOf(count));
            binding.tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            binding.tvCartBadge.setVisibility(View.GONE);
        }
    }

    private void applyFilters() {
        List<ItemModel> filtered = new ArrayList<>(allFavoriteItems);

        // Lọc theo search query
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            List<ItemModel> searchFiltered = new ArrayList<>();
            for (ItemModel item : filtered) {
                if (item.getTitle() != null
                        && item.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase())) {
                    searchFiltered.add(item);
                }
            }
            filtered = searchFiltered;
        }

        if (filterState.isInStockSelected()) {
            filtered = filterByInStock(filtered);
        }

        if (filterState.isPriceRangeSelected()) {
            filtered = filterByPriceRange(filtered, filterState.getMinPrice(), filterState.getMaxPrice());
        }

        // Lọc sản phẩm bán chạy nếu chọn popular
        if (filterState.getSortType() == FilterState.SortType.POPULAR) {
            filtered = filterByPopular(filtered);
        } else if (filterState.getSortType() == FilterState.SortType.PRICE_LOW
                || filterState.getSortType() == FilterState.SortType.PRICE_HIGH) {
            filtered = sortByPrice(filtered, filterState.getSortType());
        }

        if (adapter != null) {
            adapter.updateList(filtered);
        }

        // Hiển thị thông báo nếu không có sản phẩm
        if (filtered.isEmpty()) {
            binding.viewFavorites.setVisibility(View.GONE);
            binding.tvNoResult.setVisibility(View.VISIBLE);
        } else {
            binding.viewFavorites.setVisibility(View.VISIBLE);
            binding.tvNoResult.setVisibility(View.GONE);
        }
    }

    private List<ItemModel> filterByInStock(List<ItemModel> items) {
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : items) {
            if (item.getSizeQuantityMap() != null) {
                for (int quantity : item.getSizeQuantityMap().values()) {
                    if (quantity > 0) {
                        filtered.add(item);
                        break;
                    }
                }
            }
        }
        return filtered;
    }

    private List<ItemModel> filterByPriceRange(List<ItemModel> items, double minPrice, double maxPrice) {
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : items) {
            Double price = item.getPrice();
            if (price != null && price >= minPrice && price <= maxPrice) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<ItemModel> sortByPrice(List<ItemModel> items, FilterState.SortType sortType) {
        List<ItemModel> sorted = new ArrayList<>(items);

        if (sortType == FilterState.SortType.PRICE_LOW) {
            sorted.sort(Comparator.comparingDouble(item -> item.getPrice() != null ? item.getPrice() : 0.0));
        } else if (sortType == FilterState.SortType.PRICE_HIGH) {
            sorted.sort((a, b) -> Double.compare(b.getPrice() != null ? b.getPrice() : 0.0,
                    a.getPrice() != null ? a.getPrice() : 0.0));
        }

        return sorted;
    }

    private List<ItemModel> filterByPopular(List<ItemModel> items) {
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : items) {
            Integer sold = item.getSold();
            if (sold != null && sold >= 1000) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private void updatePriceRangeChipText(double minPrice, double maxPrice, FilterState.PriceRangeType rangeType) {
        String chipText;
        switch (rangeType) {
            case UNDER_50:
                chipText = "Dưới 500K";
                break;
            case FIFTY_TO_100:
                chipText = "500K - 1 triệu";
                break;
            case HUNDRED_TO_200:
                chipText = "1 triệu - 2 triệu";
                break;
            case OVER_200:
                chipText = "Trên 2 triệu";
                break;
            default:
                chipText = getString(R.string.chip_price_range_default);
                break;
        }
        binding.chipPriceRange.setText(chipText);
        boolean isSelected = rangeType != FilterState.PriceRangeType.NONE;
        ChipStyleUtils.applyStyle(this, binding.chipPriceRange, isSelected);
    }
}

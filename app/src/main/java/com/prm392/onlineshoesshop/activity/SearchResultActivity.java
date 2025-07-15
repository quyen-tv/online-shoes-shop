package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.PopularAdapter;
import com.prm392.onlineshoesshop.databinding.ActivitySearchResultBinding;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.model.FilterState;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ChipStyleUtils;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private ActivitySearchResultBinding binding;

    // UI Components
    private EditText etSearchResult;
    private TextView tvResultTitle, tvNoResult;
    private RecyclerView rvSearchResult;
    private Chip chipInStock, chipPriceRange, chipSortPriceLow, chipSortPriceHigh, chipSortPopular;
    private PopularAdapter popularAdapter;

    // Data
    private String searchQuery;
    private List<ItemModel> allItems = new ArrayList<>();
    private List<String> favoriteIds = new ArrayList<>();
    private FilterState filterState = new FilterState();
    private int selectedPriceIndex = 0;
    private String[] priceRanges;

    // ViewModels
    private ItemViewModel itemViewModel;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get search query from intent
        searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery == null) {
            searchQuery = "";
        }

        initViews();
        initViewModels();
        setupListeners();
        setupObservers();
        performSearch();
    }

    private void initViews() {
        etSearchResult = binding.etSearchResult;
        tvResultTitle = binding.tvResultTitle;
        tvNoResult = binding.tvNoResult;
        rvSearchResult = binding.rvSearchResult;
        chipInStock = binding.chipInStock;
        chipPriceRange = binding.chipPriceRange;
        chipSortPriceLow = binding.chipSortPriceLow;
        chipSortPriceHigh = binding.chipSortPriceHigh;
        chipSortPopular = binding.chipSortPopular;

        priceRanges = new String[] {
                getString(R.string.price_range_all),        // vị trí index 0
                getString(R.string.chip_price_range_under_50),                                 // UNDER_50
                getString(R.string.chip_price_range_50_100),                               // FIFTY_TO_100
                getString(R.string.chip_price_range_100_200),                              // HUNDRED_TO_200
                getString(R.string.chip_price_range_over_200)                                 // OVER_200
        };

        // Set search query text
        etSearchResult.setText(searchQuery);
        etSearchResult.setSelection(searchQuery.length());
        etSearchResult.setFocusable(false);
        etSearchResult.setClickable(true);
        etSearchResult.setOnClickListener(v -> {
            Intent intent = new Intent(SearchResultActivity.this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("search_text", searchQuery);
            startActivity(intent);
        });
    }

    private void initViewModels() {
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();

        ItemViewModelFactory factory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, factory).get(ItemViewModel.class);

        AuthViewModelFactory authFactory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(this, authFactory).get(AuthViewModel.class);
    }

    private void setupListeners() {
        // Back button
        binding.ivBack.setOnClickListener(v -> finish());

        // Filter: In stock
        chipInStock.setOnClickListener(v -> {
            filterState = filterState.toggleInStock();
            updateAllChipsAppearance();
            performSearch();
        });

        // Filter: Price range
        chipPriceRange.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setTitle(getString(R.string.chip_price_range_default));
            builder.setSingleChoiceItems(priceRanges, selectedPriceIndex, (dialog, which) -> {
                selectedPriceIndex = which;
                double minPrice = 0, maxPrice = Double.MAX_VALUE;
                FilterState.PriceRangeType type = FilterState.PriceRangeType.NONE;

                switch (which) {
                    case 1:
                        maxPrice = 50;
                        type = FilterState.PriceRangeType.UNDER_50;
                        break;
                    case 2:
                        minPrice = 50;
                        maxPrice = 100;
                        type = FilterState.PriceRangeType.FIFTY_TO_100;
                        break;
                    case 3:
                        minPrice = 100;
                        maxPrice = 200;
                        type = FilterState.PriceRangeType.HUNDRED_TO_200;
                        break;
                    case 4:
                        minPrice = 200;
                        maxPrice = Double.MAX_VALUE;
                        type = FilterState.PriceRangeType.OVER_200;
                        break;
                }

                if (which == 0) {
                    chipPriceRange.setText(getString(R.string.chip_price_range_default));
                    filterState = filterState.togglePriceRange();
                } else {
                    chipPriceRange.setText(priceRanges[which]);
                    filterState = filterState.setPriceRange(minPrice, maxPrice, type);
                }
                updateAllChipsAppearance();
                performSearch();
                dialog.dismiss();
            });
            builder.show();
        });

        // Sort: Price low to high
        chipSortPriceLow.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_LOW);
            updateAllChipsAppearance();
            performSearch();
        });

        // Sort: Price high to low
        chipSortPriceHigh.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_HIGH);
            updateAllChipsAppearance();
            performSearch();
        });

        // Sort: Popular
        chipSortPopular.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.POPULAR);
            updateAllChipsAppearance();
            performSearch();
        });
    }

    private void setupObservers() {
        // Observe product data
        itemViewModel.allItems.observe(this, items -> {
            if (items != null) {
                allItems = items;
                performSearch();
            }
        });

        // Observe favorite state
        itemViewModel.currentUserData.observe(this, user -> {
            if (user != null && user.getFavoriteItems() != null) {
                List<String> ids = new ArrayList<>();
                for (java.util.Map.Entry<String, Boolean> entry : user.getFavoriteItems().entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        ids.add(entry.getKey());
                    }
                }
                favoriteIds = ids;
                if (popularAdapter != null) {
                    popularAdapter.setFavoriteIds(favoriteIds);
                }
            }
        });
    }

    private void performSearch() {
        if (searchQuery.trim().isEmpty()) {
            showNoResults(getString(R.string.search_empty_query));
            return;
        }

        // Filter by search query
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : allItems) {
            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) {
                filtered.add(item);
            }
        }

        // Apply in-stock filter
        if (filterState.isInStockSelected()) {
            filtered = filterByInStock(filtered);
        }

        // Apply price range filter
        if (filterState.isPriceRangeSelected()) {
            filtered = filterByPriceRange(filtered, filterState.getMinPrice(), filterState.getMaxPrice());
        }

        // Apply sorting
        if (filterState.getSortType() == FilterState.SortType.PRICE_LOW ||
                filterState.getSortType() == FilterState.SortType.PRICE_HIGH) {
            filtered = sortByPrice(filtered, filterState.getSortType());
        }

        // Display results
        if (filtered.isEmpty()) {
            showNoResults(getString(R.string.search_no_result));
        } else {
            showResults(filtered);
        }
    }

    private void showNoResults(String message) {
        rvSearchResult.setVisibility(View.GONE);
        tvResultTitle.setVisibility(View.GONE);
        tvNoResult.setVisibility(View.VISIBLE);
        tvNoResult.setText(message);
    }

    private void showResults(List<ItemModel> items) {
        tvNoResult.setVisibility(View.GONE);
        tvResultTitle.setVisibility(View.VISIBLE);
        tvResultTitle.setText(getString(R.string.search_found_count, items.size()));

        if (popularAdapter == null) {
            popularAdapter = new PopularAdapter(items);
            popularAdapter.setFavoriteIds(favoriteIds);
            popularAdapter.setOnChangeListener(new PopularAdapter.OnChangeListener() {
                @Override
                public void onToggleFavorite(String itemId) {
                    itemViewModel.toggleFavorite(itemId);
                }

                @Override
                public void onClick(ItemModel item) {
                    Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
                    intent.putExtra("object", item);
                    startActivity(intent);
                }
            });
            rvSearchResult.setAdapter(popularAdapter);
            rvSearchResult.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            popularAdapter = new PopularAdapter(items);
            popularAdapter.setFavoriteIds(favoriteIds);
            popularAdapter.setOnChangeListener(new PopularAdapter.OnChangeListener() {
                @Override
                public void onToggleFavorite(String itemId) {
                    itemViewModel.toggleFavorite(itemId);
                }

                @Override
                public void onClick(ItemModel item) {
                    Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
                    intent.putExtra("object", item);
                    startActivity(intent);
                }
            });
            rvSearchResult.setAdapter(popularAdapter);
        }
        rvSearchResult.setVisibility(View.VISIBLE);
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
            sorted.sort((item1, item2) -> {
                Double price1 = item1.getPrice();
                Double price2 = item2.getPrice();
                return Double.compare(price1 != null ? price1 : 0.0, price2 != null ? price2 : 0.0);
            });
        } else if (sortType == FilterState.SortType.PRICE_HIGH) {
            sorted.sort((item1, item2) -> {
                Double price1 = item1.getPrice();
                Double price2 = item2.getPrice();
                return Double.compare(price2 != null ? price2 : 0.0, price1 != null ? price1 : 0.0);
            });
        }
        return sorted;
    }

    private void updateChipAppearance(Chip chip, boolean isSelected) {
        chip.setChecked(isSelected);
        ChipStyleUtils.applyStyle(this, chip, isSelected);
    }

    private void updateSortChipsAppearance() {
        boolean isLow = filterState.getSortType() == FilterState.SortType.PRICE_LOW;
        boolean isHigh = filterState.getSortType() == FilterState.SortType.PRICE_HIGH;
        boolean isPopular = filterState.getSortType() == FilterState.SortType.POPULAR;

        chipSortPriceLow.setChecked(isLow);
        chipSortPriceHigh.setChecked(isHigh);
        chipSortPopular.setChecked(isPopular);

        ChipStyleUtils.applyStyle(this, chipSortPriceLow, isLow);
        ChipStyleUtils.applyStyle(this, chipSortPriceHigh, isHigh);
        ChipStyleUtils.applyStyle(this, chipSortPopular, isPopular);
    }

    private void updateAllChipsAppearance() {
        updateChipAppearance(chipInStock, filterState.isInStockSelected());
        updateChipAppearance(chipPriceRange, filterState.isPriceRangeSelected());
        updateSortChipsAppearance();

        if (filterState.isPriceRangeSelected()) {
            updatePriceRangeChipText(filterState.getMinPrice(), filterState.getMaxPrice());
        } else {
            chipPriceRange.setText(getString(R.string.chip_price_range_default));
        }
    }

    private void updatePriceRangeChipText(double minPrice, double maxPrice) {
        String chipText;
        if (minPrice == 0 && maxPrice == Double.MAX_VALUE) {
            chipText = getString(R.string.chip_price_range_default);
        } else if (minPrice == 0 && maxPrice == 50) {
            chipText = getString(R.string.chip_price_range_under_50);
        } else if (minPrice == 50 && maxPrice == 100) {
            chipText = getString(R.string.chip_price_range_50_100);
        } else if (minPrice == 100 && maxPrice == 200) {
            chipText = getString(R.string.chip_price_range_100_200);
        } else if (minPrice == 200 && maxPrice == Double.MAX_VALUE) {
            chipText = getString(R.string.chip_price_range_over_200);
        } else {
            chipText = getString(R.string.chip_price_range_custom_range, minPrice, maxPrice);
        }
        chipPriceRange.setText(chipText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync favorite state when returning from DetailActivity
        authViewModel.reloadCurrentUser();
    }
}
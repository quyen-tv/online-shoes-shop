package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.AllItemAdapter;
import com.prm392.onlineshoesshop.adapter.CategoryAdapter;
import com.prm392.onlineshoesshop.adapter.SliderAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityAllItemsBinding;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.model.CategoryModel;
import com.prm392.onlineshoesshop.model.FilterState;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.SliderModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ChipStyleUtils;
import com.prm392.onlineshoesshop.utils.PriceRangeDialog;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.viewmodel.MainViewModel;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class AllItemsActivity extends AppCompatActivity {

    private static final String TAG = "AllItemsActivity";
    private static final int GRID_SPAN_COUNT = 2;
    private static final int BANNER_OFFSCREEN_PAGE_LIMIT = 3;
    private static final int BANNER_MARGIN = 40;

    private ActivityAllItemsBinding binding;
    private final MainViewModel mainViewModel = new MainViewModel();
    private ItemViewModel itemViewModel;
    private FilterState filterState = new FilterState();
    private String[] priceRanges;
    private int selectedPriceIndex = 0;
    private List<String> favoriteIds = new ArrayList<>();
    private AllItemAdapter allItemAdapter;
    private AuthViewModel authViewModel;
    private String selectedBrandFilter;
    private String searchQuery = "";
    private PriceRangeDialog priceRangeDialog;
    private final ExecutorService filterExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sử dụng resource string cho các mức giá
        priceRanges = new String[] {
                getString(R.string.price_range_all), // vị trí index 0
                getString(R.string.chip_price_range_under_50), // UNDER_50
                getString(R.string.chip_price_range_50_100), // FIFTY_TO_100
                getString(R.string.chip_price_range_100_200), // HUNDRED_TO_200
                getString(R.string.chip_price_range_over_200) // OVER_200
        };

        initializeDependencies();
        initializeViews();
        setupObservers();

        // Sự kiện click icon search: mở SearchActivity
        binding.ivSearchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(AllItemsActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        // Sự kiện click icon cart: mở CartActivity
        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(AllItemsActivity.this, CartActivity.class);
            startActivity(intent);
        });

        binding.ivChat.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
    }

    private void initializeDependencies() {
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel.class);
        AuthViewModelFactory authViewModelFactory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(this, authViewModelFactory).get(AuthViewModel.class);

        // Initialize price range dialog
        priceRangeDialog = new PriceRangeDialog(this, (minPrice, maxPrice, rangeType) -> {
            filterState = filterState.setPriceRange(minPrice, maxPrice, rangeType);
            updateChipAppearance(binding.chipPriceRange, true);
            updatePriceRangeChipText(minPrice, maxPrice, rangeType);
            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }
        });
    }

    private void initializeViews() {
        initBanner();
        initItemsList();
        initCategory();
        initFilterChips();
        initBackButton();
        updatePriceRangeChipStyle();
    }

    private void updatePriceRangeChipStyle() {
        boolean isDefault = selectedPriceIndex == 0;
        binding.chipPriceRange
                .setText(isDefault ? getString(R.string.chip_price_range_default) : priceRanges[selectedPriceIndex]);
        ChipStyleUtils.applyStyle(this, binding.chipPriceRange, !isDefault);
    }

    private void setupObservers() {
        observeBanners();
        observeItems();
        observeCategories();
    }

    // MARK: - Filter Management

    private void initFilterChips() {
        setupFilterChips();
        setupSortChips();
    }

    private void setupFilterChips() {
        binding.chipInStock.setOnClickListener(v -> {
            filterState = filterState.toggleInStock();
            updateChipAppearance(binding.chipInStock, filterState.isInStockSelected());

            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }

        });

        binding.chipPriceRange.setOnClickListener(v -> {
            priceRangeDialog.show(filterState.getPriceRangeType(), filterState.getMinPrice(),
                    filterState.getMaxPrice());
        });
    }

    private void showPriceRangeDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.chip_price_range_default));
        builder.setSingleChoiceItems(priceRanges, selectedPriceIndex, (dialog, which) -> {
            selectedPriceIndex = which;

            double minPrice = 0;
            double maxPrice = Double.MAX_VALUE;
            FilterState.PriceRangeType selectedRange;

            switch (which) {
                case 0: // Tất cả
                    selectedRange = FilterState.PriceRangeType.NONE;
                    break;
                case 1: // Dưới 50
                    selectedRange = FilterState.PriceRangeType.UNDER_50;
                    maxPrice = 50;
                    break;
                case 2: // 50 - 100
                    selectedRange = FilterState.PriceRangeType.FIFTY_TO_100;
                    minPrice = 50;
                    maxPrice = 100;
                    break;
                case 3: // 100 - 200
                    selectedRange = FilterState.PriceRangeType.HUNDRED_TO_200;
                    minPrice = 100;
                    maxPrice = 200;
                    break;
                case 4: // Trên 200
                    selectedRange = FilterState.PriceRangeType.OVER_200;
                    minPrice = 200;
                    maxPrice = Double.MAX_VALUE;
                    break;
                default:
                    selectedRange = FilterState.PriceRangeType.NONE;
                    break;
            }

            // Cập nhật chip text & style
            if (selectedRange == FilterState.PriceRangeType.NONE) {
                binding.chipPriceRange.setText(getString(R.string.chip_price_range_default));
                ChipStyleUtils.applyStyle(this, binding.chipPriceRange, false);
            } else {
                binding.chipPriceRange.setText(priceRanges[which]);
                ChipStyleUtils.applyStyle(this, binding.chipPriceRange, true);
            }

            // Cập nhật filterState
            if (selectedRange == FilterState.PriceRangeType.NONE) {
                filterState = filterState.togglePriceRange(); // tắt filter nếu chọn "tất cả"
            } else {
                filterState = filterState.setPriceRange(minPrice, maxPrice, selectedRange);
            }

            // Cập nhật giao diện
            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }

            dialog.dismiss();
        });
        builder.show();
    }

    private void setupSortChips() {
        binding.chipSortPriceLow.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_LOW);
            updateSortChipsAppearance();

            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }

        });

        binding.chipSortPriceHigh.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_HIGH);
            updateSortChipsAppearance();

            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }

        });

        binding.chipSortPopular.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.POPULAR);
            updateSortChipsAppearance();

            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }

        });
    }

    private void updateSortChipsAppearance() {
        updateChipAppearance(binding.chipSortPriceLow, filterState.getSortType() == FilterState.SortType.PRICE_LOW);
        updateChipAppearance(binding.chipSortPriceHigh, filterState.getSortType() == FilterState.SortType.PRICE_HIGH);
        updateChipAppearance(binding.chipSortPopular, filterState.getSortType() == FilterState.SortType.POPULAR);
    }

    private void updateChipAppearance(@NonNull Chip chip, boolean isSelected) {
        ChipStyleUtils.applyStyle(this, chip, isSelected);
    }

    private void resetAllFilters() {
        filterState = filterState.reset();
        updateAllChipsAppearance();
        selectedPriceIndex = 0;
        updatePriceRangeChipStyle();
    }

    private void updateAllChipsAppearance() {
        updateChipAppearance(binding.chipInStock, filterState.isInStockSelected());
        updateChipAppearance(binding.chipPriceRange, filterState.isPriceRangeSelected());
        updateSortChipsAppearance();

        // Update price range chip text if price range is selected
        if (filterState.isPriceRangeSelected()) {
            updatePriceRangeChipText(filterState.getMinPrice(), filterState.getMaxPrice(),
                    filterState.getPriceRangeType());
        }
    }

    private List<ItemModel> applyFilters(List<ItemModel> allItems) {
        List<ItemModel> result = new ArrayList<>(allItems);

        if (filterState.isInStockSelected()) {
            result = filterByInStock(result);
        }

        if (filterState.isPriceRangeSelected()) {
            result = filterByPriceRange(result, filterState.getMinPrice(), filterState.getMaxPrice());
        }
        Log.d("FILTER", "Price range: " + filterState.getMinPrice() + " - " + filterState.getMaxPrice());

        result = filterItemsByBrand(result, selectedBrandFilter);

        // Lọc sản phẩm bán chạy nếu chọn popular
        if (filterState.getSortType() == FilterState.SortType.POPULAR) {
            result = filterByPopular(result);
        } else if (filterState.getSortType() == FilterState.SortType.PRICE_LOW
                || filterState.getSortType() == FilterState.SortType.PRICE_HIGH) {
            result = sortByPrice(result, filterState.getSortType());
        }
        result = filterByName(result, searchQuery);

        return result;
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
            case CUSTOM:
                if (maxPrice == Double.MAX_VALUE) {
                    chipText = String.format("Từ %s+", formatVnCurrency(minPrice));
                } else {
                    chipText = String.format("%s - %s", formatVnCurrency(minPrice), formatVnCurrency(maxPrice));
                }
                break;
            default:
                chipText = "Chọn khoảng giá";
                break;
        }

        binding.chipPriceRange.setText(chipText);

        // Đổi màu chip: chỉ các mốc khác NONE mới tím
        boolean isSelected = rangeType != FilterState.PriceRangeType.NONE;
        ChipStyleUtils.applyStyle(this, binding.chipPriceRange, isSelected);
    }

    private String getPriceRangeMessage(double minPrice, double maxPrice, FilterState.PriceRangeType rangeType) {
        switch (rangeType) {
            case UNDER_50:
                return "Dưới 500K";
            case FIFTY_TO_100:
                return "500K - 1 triệu";
            case HUNDRED_TO_200:
                return "1 triệu - 2 triệu";
            case OVER_200:
                return "Trên 2 triệu";
            case CUSTOM:
                if (maxPrice == Double.MAX_VALUE) {
                    return String.format("Từ %s trở lên", formatVnCurrency(minPrice));
                } else {
                    return String.format("%s - %s", formatVnCurrency(minPrice), formatVnCurrency(maxPrice));
                }
            default:
                return "Chọn khoảng giá";
        }
    }

    private String formatVnCurrency(double value) {
        if (value >= 1_000_000) {
            return String.format("%.0f triệu", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.0fK", value / 1_000);
        } else {
            return String.format("%.0f", value);
        }
    }

    // MARK: - UI Initialization

    private void initBackButton() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void initItemsList() {
        binding.progressBarItems.setVisibility(View.VISIBLE);
    }

    private void initBanner() {
        binding.progressBarBanner.setVisibility(View.VISIBLE);
    }

    private void initCategory() {
        binding.progressBarBrand.setVisibility(View.VISIBLE);
    }

    // MARK: - Observers

    private void observeBanners() {
        mainViewModel.banners.observe(this, this::setupBanner);
    }

    private void observeItems() {
        itemViewModel.allItems.observe(this, this::setupItemsList);
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null && user.getFavoriteItems() != null) {
                List<String> ids = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : user.getFavoriteItems().entrySet()) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        ids.add(entry.getKey());
                    }
                }
                favoriteIds = ids;
                if (allItemAdapter != null) {
                    allItemAdapter.setFavoriteIds(favoriteIds);
                }
            }
        });
    }

    private void observeCategories() {
        mainViewModel.categories.observe(this, this::setupCategoryList);
    }

    // MARK: - Setup Methods

    private void setupBanner(@NonNull List<SliderModel> images) {
        binding.progressBarBanner.setVisibility(View.GONE);

        binding.viewPageSlider.setAdapter(new SliderAdapter(images));
        binding.viewPageSlider.setClipToPadding(false);
        binding.viewPageSlider.setClipChildren(false);
        binding.viewPageSlider.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        binding.viewPageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(BANNER_MARGIN));
        binding.viewPageSlider.setPageTransformer(compositePageTransformer);

        if (images.size() > 1) {
            binding.dotIndicator.setVisibility(View.VISIBLE);
            binding.dotIndicator.attachTo(binding.viewPageSlider);
        }
    }

    private void setupItemsList(@NonNull List<?> itemModels) {
        binding.progressBarItems.setVisibility(View.GONE);

        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
        binding.viewAllItems.setLayoutManager(layoutManager);

        // CAST list
        List<ItemModel> allItems = (List<ItemModel>) itemModels;

        // 💡 Áp dụng filter theo selectedBrandFilter trên background thread
        filterExecutor.execute(() -> {
            List<ItemModel> filteredItems = applyFilters(allItems);
            mainHandler.post(() -> {
                if (allItemAdapter == null) {
                    allItemAdapter = new AllItemAdapter(filteredItems);
                    allItemAdapter.setFavoriteIds(favoriteIds);
                    allItemAdapter.setOnChangeListener(new AllItemAdapter.OnChangeListener() {
                        @Override
                        public void onToggleFavorite(String itemId) {
                            // Gọi ViewModel để toggle favorite
                            itemViewModel.toggleFavorite(itemId);
                        }

                        @Override
                        public void onClick(ItemModel item) {
                            Intent intent = new Intent(AllItemsActivity.this, DetailActivity.class);
                            intent.putExtra("object", item);
                            startActivity(intent);
                        }
                    });
                    binding.viewAllItems.setAdapter(allItemAdapter);
                    int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
                    binding.viewAllItems.addItemDecoration(new SpaceItemDecoration(spacing, GRID_SPAN_COUNT));
                } else {
                    allItemAdapter.updateData(filteredItems);
                    allItemAdapter.setFavoriteIds(favoriteIds);
                }
            });
        });
    }

    private void setupCategoryList(@NonNull List<?> itemModels) {
        binding.progressBarBrand.setVisibility(View.GONE);

        binding.viewBrand.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        CategoryAdapter categoryAdapter = new CategoryAdapter((List<CategoryModel>) itemModels);
        categoryAdapter.setOnBrandSelectedListener(selectedBrand -> {
            selectedBrandFilter = selectedBrand;
            Log.d("BrandFilter", selectedBrandFilter);
            if (itemViewModel.allItems.getValue() != null) {
                setupItemsList(itemViewModel.allItems.getValue());
            }
        });

        binding.viewBrand.setAdapter(categoryAdapter);

    }

    // MARK: - Data Loading

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        authViewModel.reloadCurrentUser();
        updateCartBadge();
    }

    private void loadData() {
        mainViewModel.loadBanners();
        mainViewModel.loadCategory();
    }

    private void updateCartBadge() {
        com.prm392.onlineshoesshop.helper.ManagementCart managementCart = new com.prm392.onlineshoesshop.helper.ManagementCart(
                this);
        int count = managementCart.getCartItems().size();
        android.widget.TextView tvCartBadge = findViewById(R.id.tvCartBadge);
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(android.view.View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(android.view.View.GONE);
            }
        }
    }

    // Filter theo từng mục
    private List<ItemModel> filterItemsByBrand(List<ItemModel> allItems, String brand) {
        if (brand == null || brand.isEmpty())
            return allItems;

        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : allItems) {
            if (item.getBrand() != null && item.getBrand().equalsIgnoreCase(brand)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<ItemModel> filterByInStock(List<ItemModel> items) {
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : items) {
            if (item.getSizeQuantityMap() != null) {
                for (int quantity : item.getSizeQuantityMap().values()) {
                    if (quantity > 0) {
                        filtered.add(item);
                        break; // Thoát khỏi vòng lặp nếu có ít nhất 1 size còn hàng
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

    private List<ItemModel> filterByName(List<ItemModel> items, String query) {
        if (query == null || query.isEmpty())
            return items;

        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : items) {
            if (item.getTitle() != null && item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        return filtered;
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
}
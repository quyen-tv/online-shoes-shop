package com.prm392.onlineshoesshop.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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

import java.util.List;

public class AllItemsActivity extends AppCompatActivity {

    private static final String TAG = "AllItemsActivity";
    private static final int GRID_SPAN_COUNT = 2;
    private static final int BANNER_OFFSCREEN_PAGE_LIMIT = 3;
    private static final int BANNER_MARGIN = 40;

    private ActivityAllItemsBinding binding;
    private final MainViewModel mainViewModel = new MainViewModel();
    private ItemViewModel itemViewModel;
    private FilterState filterState = new FilterState();
    private PriceRangeDialog priceRangeDialog;
    private String[] priceRanges;
    private int selectedPriceIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Sử dụng resource string cho các mức giá
        priceRanges = new String[] {
                getString(R.string.price_range_all),
                getString(R.string.price_range_under_10m),
                getString(R.string.price_range_10_16m),
                getString(R.string.price_range_16_22m),
                getString(R.string.price_range_over_22m)
        };

        initializeDependencies();
        initializeViews();
        setupObservers();
    }

    private void initializeDependencies() {
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, itemViewModelFactory).get(ItemViewModel.class);

        // Initialize price range dialog
        priceRangeDialog = new PriceRangeDialog(this, (minPrice, maxPrice, rangeType) -> {
            filterState = filterState.setPriceRange(minPrice, maxPrice, rangeType);
            updateChipAppearance(binding.chipPriceRange, true);
            updatePriceRangeChipText(minPrice, maxPrice, rangeType);
            applyFilters();

            // Show feedback to user
            String message = getPriceRangeMessage(minPrice, maxPrice, rangeType);
            Toast.makeText(this, "Đã áp dụng: " + message, Toast.LENGTH_SHORT).show();
        });
    }

    private void initializeViews() {
        initBanner();
        initItemsList();
        initCategory();
        initFilterChips();
        initBackButton();
        // Sự kiện chọn khoảng giá đơn giản
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
                // TODO: Lọc dữ liệu theo khoảng giá đã chọn ở đây
            });
            builder.show();
        });
        // Áp dụng style đúng khi khởi tạo
        updatePriceRangeChipStyle();
    }

    private void updatePriceRangeChipStyle() {
        if (selectedPriceIndex == 0) {
            binding.chipPriceRange.setText(getString(R.string.chip_price_range_default));
            ChipStyleUtils.applyStyle(this, binding.chipPriceRange, false);
        } else {
            binding.chipPriceRange.setText(priceRanges[selectedPriceIndex]);
            ChipStyleUtils.applyStyle(this, binding.chipPriceRange, true);
        }
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
            applyFilters();
        });

        binding.chipPriceRange.setOnClickListener(v -> {
            if (filterState.isPriceRangeSelected()) {
                // If price range is already selected, reset it
                filterState = filterState.setPriceRange(0, 0, FilterState.PriceRangeType.NONE);
                updateChipAppearance(binding.chipPriceRange, false);
                binding.chipPriceRange.setText("Chọn khoảng giá");
                applyFilters();
            } else {
                // Show price range dialog with current selection
                priceRangeDialog.show(filterState.getPriceRangeType(), filterState.getMinPrice(),
                        filterState.getMaxPrice());
            }
        });
    }

    private void setupSortChips() {
        binding.chipSortPriceLow.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_LOW);
            updateSortChipsAppearance();
            applyFilters();
        });

        binding.chipSortPriceHigh.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.PRICE_HIGH);
            updateSortChipsAppearance();
            applyFilters();
        });

        binding.chipSortPopular.setOnClickListener(v -> {
            filterState = filterState.setSortType(FilterState.SortType.POPULAR);
            updateSortChipsAppearance();
            applyFilters();
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

    private void applyFilters() {
        // TODO: Implement actual filtering logic based on filterState
        // This method can be called whenever filter states change
        // Example:
        // if (filterState.isInStockSelected()) {
        // // Filter items that are in stock
        // }
        // if (filterState.isPriceRangeSelected()) {
        // // Filter items by price range
        // double minPrice = filterState.getMinPrice();
        // double maxPrice = filterState.getMaxPrice();
        // // Apply price filtering
        // }
        // if (filterState.getSortType() == FilterState.SortType.PRICE_LOW) {
        // // Sort items by price low to high
        // }

        // For now, just log the filter state
        System.out.println("Applied filters: " + filterState.toString());
    }

    private void updatePriceRangeChipText(double minPrice, double maxPrice, FilterState.PriceRangeType rangeType) {
        String chipText;

        switch (rangeType) {
            case UNDER_50:
                chipText = "Dưới $50";
                break;
            case FIFTY_TO_100:
                chipText = "$50 - $100";
                break;
            case HUNDRED_TO_200:
                chipText = "$100 - $200";
                break;
            case TWO_HUNDRED_TO_500:
                chipText = "$200 - $500";
                break;
            case OVER_500:
                chipText = "Trên $500";
                break;
            case CUSTOM:
                if (maxPrice == Double.MAX_VALUE) {
                    chipText = String.format("Từ $%.0f+", minPrice);
                } else {
                    chipText = String.format("$%.0f - $%.0f", minPrice, maxPrice);
                }
                break;
            default:
                chipText = "Chọn khoảng giá";
                break;
        }

        binding.chipPriceRange.setText(chipText);
    }

    private String getPriceRangeMessage(double minPrice, double maxPrice, FilterState.PriceRangeType rangeType) {
        switch (rangeType) {
            case UNDER_50:
                return "Dưới $50";
            case FIFTY_TO_100:
                return "$50 - $100";
            case HUNDRED_TO_200:
                return "$100 - $200";
            case TWO_HUNDRED_TO_500:
                return "$200 - $500";
            case OVER_500:
                return "Trên $500";
            case CUSTOM:
                if (maxPrice == Double.MAX_VALUE) {
                    return String.format("Từ $%.0f trở lên", minPrice);
                } else {
                    return String.format("$%.0f - $%.0f", minPrice, maxPrice);
                }
            default:
                return "Chọn khoảng giá";
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
        binding.viewAllItems.setAdapter(new AllItemAdapter((List<ItemModel>) itemModels));
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        binding.viewAllItems.addItemDecoration(new SpaceItemDecoration(spacing, GRID_SPAN_COUNT));
    }

    private void setupCategoryList(@NonNull List<?> itemModels) {
        binding.progressBarBrand.setVisibility(View.GONE);

        binding.viewBrand.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.viewBrand.setAdapter(new CategoryAdapter((List<CategoryModel>) itemModels));
    }

    // MARK: - Data Loading

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mainViewModel.loadBanners();
        mainViewModel.loadCategory();
    }

}
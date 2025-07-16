package com.prm392.onlineshoesshop.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.CategoryAdapter;
import com.prm392.onlineshoesshop.adapter.PopularAdapter;
import com.prm392.onlineshoesshop.adapter.SliderAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityMainBinding;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.SliderModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PopularAdapter.OnChangeListener {

    private final MainViewModel viewModel = new MainViewModel();
    private AuthViewModel authViewModel;
    private ItemViewModel itemViewModel;
    private ActivityMainBinding binding;
    private String selectedBrandFilter = "";
    private boolean isItemDecorationAdded = false;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserRepository userRepository = new UserRepository();
        AuthViewModelFactory authViewModelFactory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(
                this,
                authViewModelFactory)
                .get(AuthViewModel.class);
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(
                this,
                itemViewModelFactory)
                .get(ItemViewModel.class);

        initWelcome();
        initBanner();
        initPopular();
        setUpListeners();
        initCategory();
        initBottomNavigation();
        binding.ivSearchIcon.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
    }

    private void initWelcome() {
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                String userName = user.getFullName();
                binding.tvUserName.setText(userName);
            }
        });

    }

    private void initBanner() {
        binding.progressBarBanner.setVisibility(View.VISIBLE);
        viewModel.banners.observe(this, sliderModels -> {
            banners(sliderModels);
            binding.progressBarBanner.setVisibility(View.GONE);
        });
        viewModel.loadBanners();
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.populars.observe(this, itemModels -> {
            List<ItemModel> filteredItems = filterItems(itemModels, selectedBrandFilter, currentSearchQuery);

            binding.viewPopular.setLayoutManager(new GridLayoutManager(this, 2));
            PopularAdapter popularAdapter = new PopularAdapter(filteredItems);
            binding.viewPopular.setAdapter(popularAdapter);
            popularAdapter.setOnChangeListener(this);

            authViewModel.currentUserData.observe(this, user -> {
                if (user != null && user.getFavoriteItems() != null) {
                    List<String> favoriteIds = new ArrayList<>();
                    for (Map.Entry<String, Boolean> entry : user.getFavoriteItems().entrySet()) {
                        if (Boolean.TRUE.equals(entry.getValue())) {
                            favoriteIds.add(entry.getKey());
                        }
                    }
                    popularAdapter.setFavoriteIds(favoriteIds);
                }
            });

            if (!isItemDecorationAdded) {
                int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
                binding.viewPopular.addItemDecoration(new SpaceItemDecoration(spacing, 2));
                isItemDecorationAdded = true;
            }

            binding.progressBarPopular.setVisibility(View.GONE);
        });
        viewModel.loadPopulars();
    }

    private void initCategory() {
        binding.progressBarBrand.setVisibility(View.VISIBLE);
        viewModel.categories.observe(this, itemModels -> {
            binding.viewBrand.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            CategoryAdapter categoryAdapter = new CategoryAdapter(itemModels);
            categoryAdapter.setOnBrandSelectedListener(brand -> {
                selectedBrandFilter = brand;
                viewModel.loadPopulars(); // gọi lại dữ liệu
            });
            binding.viewBrand.setAdapter(categoryAdapter);
            binding.progressBarBrand.setVisibility(View.GONE);
        });
        viewModel.loadCategory();
    }

    private void banners(List<SliderModel> images) {
        binding.viewPageSlider.setAdapter(new SliderAdapter(images));
        binding.viewPageSlider.setClipToPadding(false);
        binding.viewPageSlider.setClipChildren(false);
        binding.viewPageSlider.setOffscreenPageLimit(3);
        binding.viewPageSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPageSlider.setPageTransformer(compositePageTransformer);

        if (images.size() > 1) {
            binding.dotIndicator.setVisibility(View.VISIBLE);
            binding.dotIndicator.attachTo(binding.viewPageSlider);
        }
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_explorer);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_notification) {
                startActivity(new Intent(this, CartActivity.class));
                return false;
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
            if (item.getItemId() == R.id.navigation_my_order) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setUpListeners() {
        binding.tvSeeAllRecommendation.setOnClickListener(v -> startActivity(new Intent(this, AllItemsActivity.class)));

        binding.ivBellIcon.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        binding.ivChat.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
    }

    private void updateCartBadge() {
        ManagementCart managementCart = new ManagementCart(this);
        int count = managementCart.getCartItems().size();
        TextView tvCartBadge = findViewById(R.id.tvCartBadge);
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        authViewModel.reloadCurrentUser();
    }

    @Override
    public void onToggleFavorite(String itemId) {
        itemViewModel.toggleFavorite(itemId);
    }

    @Override
    public void onClick(ItemModel item) {
        if (item == null) {
            Log.e("MainActivity", "Item is null before passing to DetailActivity");
            return;
        }
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    private List<ItemModel> filterItems(List<ItemModel> allItems, String brand, String searchQuery) {
        List<ItemModel> filtered = new ArrayList<>();
        for (ItemModel item : allItems) {
            boolean matchesBrand = true;
            boolean matchesSearch = true;
            boolean matchesSold = item.getSold() != null && item.getSold() >= 1000; // Chỉ lấy sản phẩm bán chạy
            // Filter by brand if specified
            if (brand != null && !brand.isEmpty()) {
                matchesBrand = item.getBrand() != null && item.getBrand().equalsIgnoreCase(brand);
            }
            // Filter by search query if specified
            if (searchQuery != null && !searchQuery.isEmpty()) {
                matchesSearch = item.getTitle() != null &&
                        item.getTitle().toLowerCase().contains(searchQuery.toLowerCase());
            }
            // Item must match all criteria
            if (matchesBrand && matchesSearch && matchesSold) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    // Hàm lấy danh sách sản phẩm bán chạy (sold > 1000)
    private List<ItemModel> getBestSellingItems(List<ItemModel> allItems) {
        List<ItemModel> bestSellers = new ArrayList<>();
        for (ItemModel item : allItems) {
            if (item.getSold() != null && item.getSold() > 1000) {
                bestSellers.add(item);
            }
        }
        return bestSellers;
    }

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

}
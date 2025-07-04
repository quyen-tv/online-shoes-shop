package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.prm392.onlineshoesshop.R;
import com.google.android.material.navigation.NavigationBarView;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.CategoryAdapter;
import com.prm392.onlineshoesshop.adapter.PopularAdapter;
import com.prm392.onlineshoesshop.adapter.SliderAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityMainBinding;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.model.SliderModel;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.viewmodel.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final MainViewModel viewModel = new MainViewModel();
    private AuthViewModel authViewModel;
    private ActivityMainBinding binding;
    private PopularAdapter popularAdapter;
    private ActivityResultLauncher<Intent> detailLauncher;

    private ItemViewModel itemViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserRepository userRepository = new UserRepository();
        AuthViewModelFactory authViewModelFactory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(this, authViewModelFactory).get(AuthViewModel.class);

        itemViewModel = new ViewModelProvider(
                this,
                new DetailActivity.ItemViewModelFactory(userRepository))
                .get(ItemViewModel.class);

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String changedItemId = result.getData().getStringExtra("changedItemId");
                        if (changedItemId != null) {
                            itemViewModel.forceRefreshUserData();  // Ép cập nhật lại LiveData -> Adapter sẽ auto update
                        }
                    }
                });


        initWelcome();
        initBanner();
        initPopular();

        initCategory();
        initBottomNavigation();
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
            binding.viewPopular.setLayoutManager(new GridLayoutManager(this, 2));
            popularAdapter = new PopularAdapter(itemModels, itemViewModel, this, detailLauncher);
            binding.viewPopular.setAdapter(popularAdapter);
            binding.progressBarPopular.setVisibility(View.GONE);
        });
        viewModel.loadPopulars();
    }

    private void initCategory() {
        binding.progressBarBrand.setVisibility(View.VISIBLE);
        viewModel.categories.observe(this, itemModels -> {
            binding.viewBrand.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            binding.viewBrand.setAdapter(new CategoryAdapter(itemModels));
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
            if (item.getItemId() == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));

                return false;
            }
            if(item.getItemId() == R.id.navigation_profile) {
                startActivity(new Intent(this,UserSettingsActivity.class));
                finish();
                return true;
            }
            if(item.getItemId() == R.id.navigation_favorite) {
                startActivity(new Intent(this, FavoriteActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

}
package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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
import com.prm392.onlineshoesshop.model.SliderModel;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.viewmodel.MainViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final MainViewModel viewModel = new MainViewModel();
    private AuthViewModel authViewModel;
    private ActivityMainBinding binding;

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

        initWelcome();
        initBanner();
        initPopular();

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.navigation_profile) {
                    item.setChecked(true);
                    startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));

                }
                return false;
            }
        });
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
            binding.viewPopular.setAdapter(new PopularAdapter(itemModels));
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
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }
            return false;
        });
    }

}
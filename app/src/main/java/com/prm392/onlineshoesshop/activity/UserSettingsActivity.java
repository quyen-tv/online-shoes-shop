package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivityUserSettingsBinding;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

public class UserSettingsActivity extends AppCompatActivity {
    private ActivityUserSettingsBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(
                this,
                new SignUpActivity.AuthViewModelFactory(new UserRepository())).get(AuthViewModel.class);

        setupUI();
        updateUI();
        observeLogoutState();
        initBottomNavigation();

        // Sự kiện click icon cart: mở CartActivity
        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(UserSettingsActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
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

    private void setupUI() {
        binding.btnToggleProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
        });

        binding.btnHistoryRedirect.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewHistoryActivity.class));
        });

        binding.ctrlLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void updateUI() {
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                if (!user.getFullName().isEmpty())
                    binding.tvFullName.setText(user.getFullName());
                binding.tvEmail.setText(user.getEmail());
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    authViewModel.logout(); // chỉ logout, chưa chuyển màn
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Quan sát trạng thái logout để xử lý điều hướng sau khi hoàn tất
    private void observeLogoutState() {
        authViewModel.getAuthSuccess().observe(this, isAuthenticated -> {
            if (Boolean.FALSE.equals(isAuthenticated)) {
                // Khi authSuccess chuyển thành false => đã logout xong
                Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Logout failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_explorer) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            if (item.getItemId() == R.id.navigation_notification) {
                startActivity(new Intent(this, CartActivity.class));
                finish();
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
}

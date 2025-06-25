package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.databinding.ActivityUserSettingsBinding;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
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
                new AuthViewModelFactory(new UserRepository())
        ).get(AuthViewModel.class);

        setupUI();
        observeLogoutState();
    }

    private void setupUI() {
        binding.btnToggleProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
        });

        binding.imgBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        binding.ctrlLogout.setOnClickListener(v -> {
            showLogoutDialog();
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
}

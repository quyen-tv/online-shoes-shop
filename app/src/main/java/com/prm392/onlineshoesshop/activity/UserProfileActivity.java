package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivityUserProfileBinding;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivityUserProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        UserRepository userRepository = new UserRepository();
        authViewModel = new ViewModelProvider(
                this,
                new SignUpActivity.AuthViewModelFactory(userRepository)
        ).get(AuthViewModel.class);

        //gán dữ liệu người dùng
        updateUI();
        setupEditButtons(); // <-- thêm dòng này

    }
    private void updateUI() {
        // Lắng nghe dữ liệu người dùng
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                binding.editTextFullName.setText(user.getFullName());
                binding.editTextEmail.setText(user.getEmail());
                binding.editTextCity.setText(user.getAddress().getCity());
                binding.editTextCountry.setText(user.getAddress().getCountry());
                binding.editTextStreet.setText(user.getAddress().getStreet());
                binding.editTextDistrict.setText(user.getAddress().getDistrict());
                binding.editTextWard.setText(user.getAddress().getWard());
                // Thêm ảnh hoặc các field khác nếu cần
            }
        });
    }

    private void enableEditing(boolean isEnabled) {
        binding.editTextFullName.setEnabled(isEnabled);
        binding.editTextEmail.setEnabled(false); // Email thường không cho chỉnh sửa
        binding.editTextCity.setEnabled(isEnabled);
        binding.editTextCountry.setEnabled(isEnabled);
        binding.editTextStreet.setEnabled(isEnabled);
        binding.editTextDistrict.setEnabled(isEnabled);
        binding.editTextWard.setEnabled(isEnabled);

        binding.buttonConfirm.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        binding.buttonCancel.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        binding.buttonUpdate.setVisibility(isEnabled ? View.GONE : View.VISIBLE);
    }

    private void setupEditButtons() {
        binding.buttonUpdate.setOnClickListener(v -> enableEditing(true));
        binding.imgBack.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, UserSettingsActivity.class));
        });
        binding.buttonCancel.setOnClickListener(v -> {
            updateUI();              // Khôi phục dữ liệu gốc
            enableEditing(false);    // Khóa lại các field
        });

        binding.buttonConfirm.setOnClickListener(v -> {
            saveUserUpdates();
            enableEditing(false);
        });
    }

    private void saveUserUpdates() {
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                String userId = user.getUid();
                Map<String, Object> updates = new HashMap<>();

                // Lấy dữ liệu từ UI và cập nhật vào map
                updates.put("fullName", binding.editTextFullName.getText().toString().trim());

                Map<String, Object> addressMap = new HashMap<>();
                addressMap.put("city", binding.editTextCity.getText().toString().trim());
                addressMap.put("country", binding.editTextCountry.getText().toString().trim());
                addressMap.put("street", binding.editTextStreet.getText().toString().trim());
                addressMap.put("district", binding.editTextDistrict.getText().toString().trim());
                addressMap.put("ward", binding.editTextWard.getText().toString().trim());

                updates.put("address", addressMap);

                new UserRepository().updateUserProfile(userId, updates)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            }
        });
    }



}

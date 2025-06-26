package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivityUserProfileBinding;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ValidationUtils;
import com.prm392.onlineshoesshop.viewmodel.AddressViewModel;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;
    private ActivityUserProfileBinding binding;
    private AddressViewModel addressViewModel;

    List<String> defaultDistricts;
    List<String> defaultWards;
    List<String> defaultCities;
    private  boolean isCityClicked = false;
    private  boolean isDistrictClicked = false;
    private  boolean isWardClicked = false;

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

        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);


        setupEditButtons();
        enableEditing(false);
        updateUI();
        setupTextWatchers();
        observeAddressData();
        observeSelectionEvents();


    }
    private void updateUI() {
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                binding.editTextFullName.setText(user.getFullName());
                binding.editTextEmail.setText(user.getEmail());
                binding.editTextCountry.setText(user.getAddress().getCountry());
                binding.editTextStreet.setText(user.getAddress().getStreet());

                addressViewModel.setSelectedCity(user.getAddress().getCity());
                addressViewModel.setSelectedDistrict(user.getAddress().getDistrict());
                addressViewModel.setSelectedWard(user.getAddress().getWard());

                // Chỉ gọi khi đã có dữ liệu user
                loadUserAddressData();
            }
        });
    }

    private void enableEditing(boolean isEnabled) {
        // EditText
        binding.editTextFullName.setEnabled(isEnabled);
        binding.editTextEmail.setEnabled(false);
        binding.editTextCountry.setEnabled(isEnabled);
        binding.editTextStreet.setEnabled(isEnabled);

        // Spinner
        toggleSpinners(isEnabled);

        // Buttons
        binding.buttonConfirm.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        binding.buttonCancel.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        binding.buttonUpdate.setVisibility(isEnabled ? View.GONE : View.VISIBLE);

        // Nếu người dùng bật chế độ chỉnh sửa thì gọi API city
        if (isEnabled && !isCityClicked) {
            isCityClicked = true;
            addressViewModel.fetchCities();
        }
    }

    private void toggleSpinners(boolean isEnabled) {
        binding.spinnerCity.setEnabled(isEnabled);
        binding.spinnerCity.setClickable(isEnabled);
        binding.spinnerDistrict.setEnabled(isEnabled);
        binding.spinnerDistrict.setClickable(isEnabled);
        binding.spinnerWard.setEnabled(isEnabled);
        binding.spinnerWard.setClickable(isEnabled);

        Drawable overlay = isEnabled ? null : ContextCompat.getDrawable(this, R.drawable.spinner_disabled_overlay);
        binding.spinnerCityContainer.setForeground(overlay);
        binding.spinnerDistrictContainer.setForeground(overlay);
        binding.spinnerWardContainer.setForeground(overlay);
    }
    private void setupEditButtons() {
        binding.buttonUpdate.setOnClickListener(v -> enableEditing(true));
        binding.imgBack.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, UserSettingsActivity.class));
        });
        binding.buttonCancel.setOnClickListener(v -> {
            updateUI();              // Khôi phục dữ liệu gốc
            resetToUserAddress();    // Gán lại adapter mặc định
            enableEditing(false);    // Khóa lại các field
        });


        binding.buttonConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserUpdates();
                enableEditing(false);
            }
        });

    }
    private void saveUserUpdates() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        Map<String, Object> updates = new HashMap<>();

        updates.put("fullName", binding.editTextFullName.getText().toString().trim());

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("city", addressViewModel.getSelectedCity().getValue());
        addressMap.put("country", addressViewModel.getCountry().getValue());
        addressMap.put("street", binding.editTextStreet.getText().toString().trim());
        addressMap.put("district", addressViewModel.getSelectedDistrict().getValue());
        addressMap.put("ward", addressViewModel.getSelectedWard().getValue());

        updates.put("address", addressMap);

        new UserRepository().updateUserProfile(userId, updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String fullName = binding.editTextFullName.getText().toString().trim();
        String city = addressViewModel.getSelectedCity().getValue();
        String district = addressViewModel.getSelectedDistrict().getValue();
        String ward = addressViewModel.getSelectedWard().getValue();
        String street = binding.editTextStreet.getText().toString().trim();

        // Kiểm tra họ tên
        if (ValidationUtils.isFieldEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else if (ValidationUtils.containsWhitespace(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_no_spaces_email));
            isValid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        // Kiểm tra địa chỉ
        if (city == null || city.equals("Select City")) {
            Toast.makeText(this, "Vui lòng chọn Thành phố", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (district == null || district.equals("Select District")) {
            Toast.makeText(this, "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (ward == null || ward.equals("Select Ward")) {
            Toast.makeText(this, "Vui lòng chọn Phường/Xã", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (ValidationUtils.isFieldEmpty(street)) {
            binding.editTextStreet.setError("Vui lòng nhập tên đường");
            isValid = false;
        } else {
            binding.editTextStreet.setError(null);
        }

        return isValid;
    }

    private void setupTextWatchers() {
        binding.editTextFullName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserAddressData() {
        if (authViewModel.currentUserData.getValue() == null
                || authViewModel.currentUserData.getValue().getAddress() == null) {
            return;
        }

        Address address = authViewModel.currentUserData.getValue().getAddress();

        defaultCities = new ArrayList<>();
        defaultDistricts = new ArrayList<>();
        defaultWards = new ArrayList<>();

        defaultCities.add(address.getCity());
        defaultDistricts.add(address.getDistrict());
        defaultWards.add(address.getWard());

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, defaultCities);
        binding.spinnerCity.setAdapter(cityAdapter);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, defaultDistricts);
        binding.spinnerDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, defaultWards);
        binding.spinnerWard.setAdapter(wardAdapter);
    }


    private void observeAddressData() {
        addressViewModel.getCities().observe(this, cities -> {
            binding.spinnerCity.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, cities));
        });
        addressViewModel.getDistricts().observe(this, districts -> {
            binding.spinnerDistrict.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, districts));
        });
        addressViewModel.getWards().observe(this, wards -> {
            binding.spinnerWard.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_item, wards));
        });
    }


    private void resetToUserAddress() {
        isCityClicked = false;
        isDistrictClicked = false;
        isWardClicked = false;
        loadUserAddressData();
    }

    private void observeSelectionEvents() {
        binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (!selected.equals(addressViewModel.getSelectedCity().getValue())) {
                    addressViewModel.setSelectedCity(selected);

                    // 👉 Reset các spinner phụ thuộc
                    addressViewModel.setSelectedDistrict(null);
                    addressViewModel.setSelectedWard(null);
                    isDistrictClicked = true;  // đánh dấu đã dùng API
                    isWardClicked = false;     // reset lại ward

                    addressViewModel.fetchDistricts(selected); // 🔁 Tự động load lại District
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (!selected.equals(addressViewModel.getSelectedDistrict().getValue())) {
                    addressViewModel.setSelectedDistrict(selected);

                    // 👉 Reset ward và gọi API
                    addressViewModel.setSelectedWard(null);
                    isWardClicked = true;

                    addressViewModel.fetchWards(selected); // 🔁 Tự động load lại Ward
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (!selected.equals(addressViewModel.getSelectedWard().getValue())) {
                    addressViewModel.setSelectedWard(selected);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

}

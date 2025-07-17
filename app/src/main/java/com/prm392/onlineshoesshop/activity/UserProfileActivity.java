package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivityUserProfileBinding;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;
import com.prm392.onlineshoesshop.viewmodel.AddressViewModel;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    private ActivityUserProfileBinding binding;
    private AuthViewModel authViewModel;
    private AddressViewModel addressViewModel;
    private Address originalAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(
                this,
                new SignUpActivity.AuthViewModelFactory(new UserRepository())
        ).get(AuthViewModel.class);

        addressViewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        setupEditButtons();
        setupTextWatchers();
        enableEditing(false);
        loadUserInfo();
    }

    private void loadUserInfo() {
        authViewModel.currentUserData.observe(this, user -> {
            if (user != null) {
                binding.editTextFullName.setText(user.getFullName());
                binding.editTextEmail.setText(user.getEmail());
                binding.editTextPhone.setText(user.getPhoneNumber());

                Address address = user.getAddress();
                if (address != null) {
                    originalAddress = new Address(address);
                    binding.editTextStreet.setText(address.getStreet());

                    addressViewModel.setSelectedCity(address.getCity().getName(), address.getCity().getCode());
                    addressViewModel.setSelectedDistrict(address.getDistrict().getName(), address.getDistrict().getCode());
                    addressViewModel.setSelectedWard(address.getWard().getName(), address.getWard().getCode());
                }

                // ✅ Sau khi user có dữ liệu → setup spinner mặc định
                setupDefaultSpinners();
            }
        });
    }


    private void setupEditButtons() {
        binding.buttonUpdate.setOnClickListener(v -> {
            enableEditing(true);
            observeAndBindSpinners();
        });

        binding.imgBack.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, UserSettingsActivity.class));
            finish();
        });

        binding.buttonCancel.setOnClickListener(v -> {
            if (originalAddress != null) {
                addressViewModel.setSelectedCity(originalAddress.getCity().getName(), originalAddress.getCity().getCode());
                addressViewModel.setSelectedDistrict(originalAddress.getDistrict().getName(), originalAddress.getDistrict().getCode());
                addressViewModel.setSelectedWard(originalAddress.getWard().getName(), originalAddress.getWard().getCode());
                binding.editTextStreet.setText(originalAddress.getStreet());
            }

            // ❗ Reset lỗi
            binding.tilFullName.setError(null);
            binding.tilPhone.setError(null);
            binding.editTextStreet.setError(null);

            enableEditing(false);
        });

        binding.buttonConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserUpdates();
                enableEditing(false);
            }
        });
    }

    private void enableEditing(boolean enable) {
        binding.editTextFullName.setEnabled(enable);
        binding.editTextPhone.setEnabled(enable);
        binding.editTextStreet.setEnabled(enable);
        binding.spinnerCity.setEnabled(enable);
        binding.spinnerDistrict.setEnabled(enable);
        binding.spinnerWard.setEnabled(enable);
        binding.buttonConfirm.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.buttonCancel.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.buttonUpdate.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void setupTextWatchers() {
        binding.editTextFullName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }
            public void afterTextChanged(Editable s) {}
        });

        binding.editTextPhone.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPhone.setError(null);
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void saveUserUpdates() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", binding.editTextFullName.getText().toString().trim());
        updates.put("phoneNumber", binding.editTextPhone.getText().toString().trim());

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("street", binding.editTextStreet.getText().toString().trim());
        addressMap.put("country", addressViewModel.getCountry().getValue());
        addressMap.put("city", addressViewModel.getSelectedCity().getValue());
        addressMap.put("district", addressViewModel.getSelectedDistrict().getValue());
        addressMap.put("ward", addressViewModel.getSelectedWard().getValue());

        updates.put("address", addressMap);

        new UserRepository().updateUserProfile(user.getUid(), updates)
                .addOnSuccessListener(aVoid -> UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Cập nhật thành công!",
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.success_green)))
                .addOnFailureListener(e -> UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Lỗi: " + e.getMessage(),
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.warning_orange)));
    }

    private boolean validateInputs() {
        boolean valid = true;

        String name = binding.editTextFullName.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String street = binding.editTextStreet.getText().toString().trim();

        // Kiểm tra họ tên
        if (ValidationUtils.isFieldEmpty(name)) {
            binding.tilFullName.setError("Vui lòng nhập họ tên");
            valid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        // Kiểm tra số điện thoại
        if (ValidationUtils.isFieldEmpty(phone) || !ValidationUtils.isValidPhoneNumber(phone)) {
            binding.tilPhone.setError("Số điện thoại không hợp lệ");
            valid = false;
        } else {
            binding.tilPhone.setError(null);
        }

        // Kiểm tra tên đường
        if (ValidationUtils.isFieldEmpty(street)) {
            binding.editTextStreet.setError("Vui lòng nhập tên đường");
            valid = false;
        } else {
            binding.editTextStreet.setError(null);
        }

        // Kiểm tra thành phố
        Address.City selectedCity = addressViewModel.getSelectedCity().getValue();
        if (selectedCity == null || selectedCity.getCode() <= 0) {
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Vui lòng chọn Thành phố",
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.warning_orange));
            valid = false;
        }

        // Kiểm tra quận/huyện
        Address.District selectedDistrict = addressViewModel.getSelectedDistrict().getValue();
        if (selectedDistrict == null || selectedDistrict.getCode() <= 0) {
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Vui lòng chọn Quận/Huyện",
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.warning_orange));
            valid = false;
        }

        // Kiểm tra phường/xã
        Address.Ward selectedWard = addressViewModel.getSelectedWard().getValue();
        if (selectedWard == null || selectedWard.getCode() <= 0) {
            UiUtils.showSnackbarWithBackground(
                    binding.getRoot(),
                    "Vui lòng chọn Phường/Xã",
                    Snackbar.LENGTH_LONG,
                    getResources().getColor(R.color.warning_orange));
            valid = false;
        }

        return valid;
    }

    private void setupDefaultSpinners() {
        User user = authViewModel.currentUserData.getValue();
        Address userAddress = (user != null) ? user.getAddress() : null;

        // City
        List<String> cityOptions = new ArrayList<>();
        cityOptions.add("Chọn Thành phố");
        if (userAddress != null && !userAddress.getCity().getName().isEmpty()) {
            cityOptions.clear();
            cityOptions.add(userAddress.getCity().getName());
        }
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityOptions);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCity.setAdapter(cityAdapter);
        binding.spinnerCity.setSelection(0);

        // District
        List<String> districtOptions = new ArrayList<>();
        districtOptions.add("Chọn Quận/Huyện");
        if (userAddress != null && !userAddress.getDistrict().getName().isEmpty()) {
            districtOptions.clear();
            districtOptions.add(userAddress.getDistrict().getName());
        }
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, districtOptions);
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerDistrict.setAdapter(districtAdapter);
        binding.spinnerDistrict.setSelection(0);

        // Ward
        List<String> wardOptions = new ArrayList<>();
        wardOptions.add("Chọn Phường/Xã");
        if (userAddress != null && !userAddress.getWard().getName().isEmpty()) {
            wardOptions.clear();
            wardOptions.add(userAddress.getWard().getName());
        }
        ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wardOptions);
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerWard.setAdapter(wardAdapter);
        binding.spinnerWard.setSelection(0);
    }

    private void observeAndBindSpinners() {
        // City
        addressViewModel.fetchCities();
        addressViewModel.getCities().observe(this, cities -> {
            List<String> cityNames = new ArrayList<>();
            cityNames.add("Chọn Thành phố");
            for (Address.City c : cities) cityNames.add(c.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cityNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCity.setAdapter(adapter);

            Address.City selected = addressViewModel.getSelectedCity().getValue();
            if (selected != null) {
                int position = cityNames.indexOf(selected.getName());
                if (position >= 0) binding.spinnerCity.setSelection(position);
            }

            binding.spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        addressViewModel.setSelectedCity(null, -1);
                        return;
                    }
                    Address.City chosen = cities.get(position - 1);
                    addressViewModel.setSelectedCity(chosen.getName(), chosen.getCode());
                    addressViewModel.fetchDistricts(chosen.getCode());
                }
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        // District
        addressViewModel.getDistricts().observe(this, districts -> {
            List<String> names = new ArrayList<>();
            names.add("Chọn Quận/Huyện"); // for districts
            for (Address.District d : districts) names.add(d.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerDistrict.setAdapter(adapter);

            Address.District selected = addressViewModel.getSelectedDistrict().getValue();
            if (selected != null) {
                int position = names.indexOf(selected.getName());
                if (position >= 0) binding.spinnerDistrict.setSelection(position);
            }

            binding.spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        addressViewModel.setSelectedDistrict(null, -1);
                        return;
                    }
                    Address.District chosen = districts.get(position - 1);
                    addressViewModel.setSelectedDistrict(chosen.getName(), chosen.getCode());
                    addressViewModel.fetchWards(chosen.getCode());
                }
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });

        // Ward
        addressViewModel.getWards().observe(this, wards -> {
            List<String> names = new ArrayList<>();
            names.add("Chọn Phường/Xã"); // for wards
            for (Address.Ward w : wards) names.add(w.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerWard.setAdapter(adapter);

            Address.Ward selected = addressViewModel.getSelectedWard().getValue();
            if (selected != null) {
                int position = names.indexOf(selected.getName());
                if (position >= 0) binding.spinnerWard.setSelection(position);
            }

            binding.spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        addressViewModel.setSelectedWard(null, -1);
                        return;
                    }
                    Address.Ward chosen = wards.get(position - 1);
                    addressViewModel.setSelectedWard(chosen.getName(), chosen.getCode());
                }
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        });
    }
}
package com.prm392.onlineshoesshop.factory;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

/**
 * Lớp ViewModelProvider.Factory tùy chỉnh.
 * Sử dụng để cung cấp một instance của AuthViewModel
 * với một UserRepository đã được khởi tạo.
 * Điều này cho phép AuthViewModel nhận các dependencies cần thiết thông qua constructor của nó.
 */
public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;

    public AuthViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
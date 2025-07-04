package com.prm392.onlineshoesshop.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

public class ItemViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;

    public ItemViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ItemViewModel.class)) {
            return (T) new ItemViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }

}
package com.prm392.onlineshoesshop.factory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

public class ItemViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemViewModelFactory(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ItemViewModel.class)) {
            return (T) new ItemViewModel(userRepository, itemRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

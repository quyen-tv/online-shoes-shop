package com.prm392.onlineshoesshop.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.model.ItemModel;

import java.util.ArrayList;
import java.util.List;

public class ItemRepository {

    private final DatabaseReference itemsRef;
    private final MutableLiveData<List<ItemModel>> _allItems = new MutableLiveData<>();
    public LiveData<List<ItemModel>> getAllItems() {
        return _allItems;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public ItemRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        itemsRef = database.getReference("Items");
        listenForItemsChanges();
    }

    private void listenForItemsChanges() {
        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItemModel> items = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItemModel item = snapshot.getValue(ItemModel.class);
                    if (item != null) {
                        item.setItemId(snapshot.getKey());
                        items.add(item);
                    }
                }
                _allItems.setValue(items);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                _errorMessage.setValue("Lỗi đọc dữ liệu: " + databaseError.getMessage());
                _allItems.setValue(new ArrayList<>());
            }
        });
    }
}

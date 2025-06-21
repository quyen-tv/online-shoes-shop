package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.model.SliderModel;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private MutableLiveData<List<SliderModel>> _banner = new MutableLiveData<>();
    public LiveData<List<SliderModel>> banners = _banner;

    public MainViewModel() {
        loadBanners();
    }

    public void loadBanners() {
        DatabaseReference Ref = firebaseDatabase.getReference("Banner");

        Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<SliderModel> lists = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    SliderModel sliderModel = childSnapshot.getValue(SliderModel.class);
                    if (sliderModel != null) {
                        lists.add(sliderModel);
                    }
                }
                _banner.setValue(lists);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

}

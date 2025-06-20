package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.adapter.BrandAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityMainBinding;
import com.prm392.onlineshoesshop.model.Brand;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<Brand> brandList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private ActivityMainBinding binding;
    private String TAG = "MainActivity";
    private BrandAdapter brandAdapter = new BrandAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.viewBrand.setAdapter(brandAdapter);
        binding.viewBrand.setLayoutManager(new GridLayoutManager(this, 4));


        databaseReference = FirebaseDatabase.getInstance().getReference("Category");

        fetchBrands();
    }

    private void fetchBrands() {

        // Use addListenerForSingleValueEvent for a one-time read
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Brand> brandList = new ArrayList<>();
                // The dataSnapshot is at the "Category" node. Iterate through its children (0, 1, 2...)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Automatically convert the child snapshot into a Brand object
                    Brand brand = snapshot.getValue(Brand.class);
                    if (brand != null) {
                        brandList.add(brand);
                    }
                }

                // Now you have a populated list of Brand objects.
                // You can use it to update your UI (e.g., a RecyclerView) or just log it.
                for (Brand brand : brandList) {
                    Log.d(TAG, "Fetched Brand: Title=" + brand.getTitle() + ", Image URL=" + brand.getPicUrl());
                }

                brandAdapter.updateBrands(brandList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateBrandUI(List<Brand> brandList) {
        BrandAdapter adapter = new BrandAdapter();
    }
}
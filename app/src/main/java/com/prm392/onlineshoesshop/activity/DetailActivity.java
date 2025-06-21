    package com.prm392.onlineshoesshop.activity;

    import android.os.Bundle;
    import android.view.View;

    import androidx.activity.EdgeToEdge;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.viewpager2.widget.ViewPager2;

    import com.prm392.onlineshoesshop.R;
    import com.prm392.onlineshoesshop.adapter.ColorAdapter;
    import com.prm392.onlineshoesshop.adapter.SizeAdapter;
    import com.prm392.onlineshoesshop.adapter.SliderAdapter;
    import com.prm392.onlineshoesshop.databinding.ActivityDetailBinding;
    import com.prm392.onlineshoesshop.helper.ManagementCart;
    import com.prm392.onlineshoesshop.model.ItemModel;
    import com.prm392.onlineshoesshop.model.SliderModel;

    import java.util.ArrayList;
    import java.util.List;

    public class DetailActivity extends AppCompatActivity {

        private ActivityDetailBinding binding;
        private ItemModel item;
        private int numberOrder = 1;
        private ManagementCart managementCart;

        private ColorAdapter colorAdapter;
        private SliderAdapter sliderAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityDetailBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            managementCart = new ManagementCart(this);

            getBundle();
            banners();
            initLists();

            setupSynchronization();
        }

        private void initLists() {
            List<String> sizeList = new ArrayList<>(item.getSize());

            binding.sizeList.setAdapter(new SizeAdapter(sizeList));
            binding.sizeList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            List<String> colorList = new ArrayList<>(item.getPicUrl());
            colorAdapter = new ColorAdapter(colorList, position -> {
                binding.slider.setCurrentItem(position, true);
            });

            binding.colorList.setAdapter(colorAdapter);
            binding.colorList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            if (!colorList.isEmpty()) {
                colorAdapter.setSelectedPosition(0);
            }
        }

        private void banners() {
            List<SliderModel> sliderItems = new ArrayList<>();
            for (String imageUrl : item.getPicUrl()) {
                sliderItems.add(new SliderModel(imageUrl));
            }

            sliderAdapter = new SliderAdapter(sliderItems);
            binding.slider.setAdapter(sliderAdapter);
            binding.slider.setClipToPadding(true);
            binding.slider.setClipChildren(true);
            binding.slider.setOffscreenPageLimit(1);

            if (sliderItems.size() > 1) {
                binding.dotIndicator.setVisibility(View.VISIBLE);
                binding.dotIndicator.attachTo(binding.slider);
            }
        }

        private void getBundle() {
            item = getIntent().getParcelableExtra("object");
            binding.tvTitle.setText(item.getTitle());
            binding.tvDescription.setText(item.getDescription());
            binding.tvPrice.setText(String.format("$%.2f",item.getPrice()));
            binding.tvRating.setText(String.valueOf(item.getRating()));
            binding.btnAddToCart.setOnClickListener(v -> {
                item.setNumberInCart(numberOrder);
                managementCart.insertFood(item);
            });
            binding.btnBack.setOnClickListener(v -> finish());
        }

        private void setupSynchronization() {
            binding.slider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (colorAdapter != null) {
                        colorAdapter.setSelectedPosition(position);
                        binding.colorList.scrollToPosition(position);
                    }
                }
            });
        }
    }
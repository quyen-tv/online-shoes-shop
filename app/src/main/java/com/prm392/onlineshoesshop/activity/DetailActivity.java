package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.adapter.ColorAdapter;
import com.prm392.onlineshoesshop.adapter.SizeAdapter;
import com.prm392.onlineshoesshop.adapter.SliderAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityDetailBinding;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.helper.ManagementCart;
import com.prm392.onlineshoesshop.helper.TinyDB;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.SliderModel;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ItemModel item;
    private int numberOrder = 1;
    private ManagementCart managementCart;
    private SizeAdapter sizeAdapter; // ✅ thêm dòng này

    private ColorAdapter colorAdapter;
    private SliderAdapter sliderAdapter;

    private ItemViewModel itemViewModel;
    private TinyDB tinyDB; // Thêm biến TinyDB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managementCart = new ManagementCart(this);
        tinyDB = new TinyDB(this);
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(
                this,
                itemViewModelFactory)
                .get(ItemViewModel.class);

        getBundle();
        if (item == null)
            return;

        saveViewHistory(item);

        banners();
        initLists();
        banners();
        initLists();
        setupObservers();

        // Sự kiện click icon cart: mở CartActivity
        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, CartActivity.class);
            startActivity(intent);
        });

        setupSynchronization();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void updateCartBadge() {
        ManagementCart managementCart = new ManagementCart(this);
        int count = managementCart.getCartItems().size();
        android.widget.TextView tvCartBadge = findViewById(R.id.tvCartBadge);
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(String.valueOf(count));
                tvCartBadge.setVisibility(android.view.View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void initLists() {
        sizeAdapter = new SizeAdapter(item.getSizeQuantityMap()); // ✅ thay vì tạo biến cục bộ
        binding.sizeList.setAdapter(sizeAdapter);

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
        if (item == null || item.getItemId() == null) {
            Log.e("DetailActivity", "Received null item or itemId from intent" + item.getItemId());
            Toast.makeText(this, "Error: Item is null or missing ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvTitle.setText(item.getTitle());
        binding.tvDescription.setText(item.getDescription());
        binding.tvPrice.setText(String.format("$%.2f", item.getPrice()));
        binding.tvRating.setText(String.valueOf(item.getRating()));
        binding.btnAddToCart.setOnClickListener(v -> {
            String selectedSize = sizeAdapter.getSelectedSize();
            if (selectedSize == null) {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Please select a size!",
                        Snackbar.LENGTH_SHORT,
                        getResources().getColor(R.color.orange));
                return;
            }
            item.setNumberInCart(numberOrder); // vẫn giữ
            managementCart.insertItem(item, selectedSize, numberOrder); // ✅ dùng đúng hàm
            startActivity(new Intent(this, CartActivity.class));
            finish();
        });

        binding.btnBack.setOnClickListener(v -> {
            finish();
        });
        // binding.btnFavorite.setOnClickListener(v -> {
        // itemViewModel.toggleFavorite(item.getItemId());
        // });
    }

    /**
     * Thiết lập các Observer để theo dõi sự thay đổi của LiveData từ AuthViewModel.
     * - isLoading: Hiển thị/ẩn ProgressBar và vô hiệu hóa/kích hoạt các phần tử UI.
     * - errorMessage: Hiển thị Snackbar với thông báo lỗi nếu có.
     * - authSuccess: Đặt lại form nếu đăng nhập/đăng ký thành công.
     * - currentUserData: Nếu đăng nhập thành công và có dữ liệu người dùng, chuyển
     * đến MainActivity.
     */
    private void setupObservers() {

        itemViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        message,
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.error_red));
            }
        });

        // itemViewModel.isItemFavorite(item.getItemId()).observe(this, isFav -> {
        // if (isFav) {
        // binding.btnFavorite.setImageDrawable(AppCompatResources.getDrawable(this,
        // R.drawable.fav_icon_fill));
        // } else {
        // binding.btnFavorite.setImageDrawable(AppCompatResources.getDrawable(this,
        // R.drawable.fav_icon));
        // }
        // });
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

    // Thêm hàm lưu lịch sử xem sản phẩm
    private void saveViewHistory(ItemModel viewedItem) {
        ArrayList<ItemModel> historyList = tinyDB.getListObject("ViewHistoryList");
        if (historyList == null)
            historyList = new ArrayList<>();
        // Xóa sản phẩm nếu đã có trong lịch sử (tránh trùng lặp)
        for (int i = 0; i < historyList.size(); i++) {
            if (historyList.get(i).getItemId().equals(viewedItem.getItemId())) {
                historyList.remove(i);
                break;
            }
        }
        // Thêm sản phẩm mới vào đầu danh sách
        historyList.add(0, viewedItem);
        // Giới hạn số lượng lịch sử (20 sản phẩm)
        if (historyList.size() > 20) {
            historyList = new ArrayList<>(historyList.subList(0, 20));
        }
        tinyDB.putListObject("ViewHistoryList", historyList);
    }
}
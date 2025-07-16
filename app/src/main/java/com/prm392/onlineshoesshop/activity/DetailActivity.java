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
import androidx.recyclerview.widget.RecyclerView;
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
import com.prm392.onlineshoesshop.adapter.AllItemAdapter;

import androidx.recyclerview.widget.GridLayoutManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.FrameLayout;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ItemModel item;
    private int numberOrder = 1;
    private ManagementCart managementCart;

    private ColorAdapter colorAdapter;
    private SliderAdapter sliderAdapter;

    private ItemViewModel itemViewModel;
    private TinyDB tinyDB;

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

        binding.cartIconContainer.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, CartActivity.class);
            startActivity(intent);
        });

        setupSynchronization();
        setupSimilarProducts();
        // Sự kiện click nút Mua ngay
        binding.lnlButtons.findViewById(R.id.btnBuyNow).setOnClickListener(v -> {
            showAddToCartBottomSheet(true);
        });
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
        colorAdapter = new ColorAdapter(new ArrayList<>(item.getPicUrl()), position -> {
            binding.slider.setCurrentItem(position, true);
        });
        binding.colorList.setAdapter(colorAdapter);
        binding.colorList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        if (!item.getPicUrl().isEmpty()) {
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
        Integer sold = item.getSold();
        String soldText;
        if (sold == null) {
            soldText = "Đã bán: 0";
        } else if (sold >= 1000) {
            int soldK = sold / 1000;
            soldText = String.format("Đã bán %dk+", soldK);
        } else {
            soldText = String.format("Đã bán: %d", sold);
        }
        binding.tvSold.setText(soldText);
        binding.tvDescription.setText(item.getDescription());
        try {
            String priceStr = String.valueOf(item.getPrice());
            double price = Double.parseDouble(priceStr);
            NumberFormat format = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
            binding.tvPrice.setText(String.format("₫%s", format.format(price)));
            binding.tvPriceBtn.setText(String.format("₫%s", format.format(price)));
        } catch (Exception e) {
            binding.tvPrice.setText(String.format("₫%s", item.getPrice()));
            binding.tvPriceBtn.setText(String.format("₫%s", item.getPrice()));
        }
        binding.tvRating.setText(String.valueOf(item.getRating()));
        binding.btnAddToCart.setOnClickListener(v -> {
            showAddToCartBottomSheet();
        });

        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnFavorite.setOnClickListener(v -> itemViewModel.toggleFavorite(item.getItemId()));
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

        itemViewModel.isItemFavorite(item.getItemId()).observe(this, isFav -> {
            if (isFav) {
                binding.btnFavorite.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_fav_fill));
                binding.btnFavorite.setImageTintList(getResources().getColorStateList(R.color.purple));
            } else {
                binding.btnFavorite.setImageDrawable(AppCompatResources.getDrawable(this,
                        R.drawable.ic_fav));
            }
        });
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

    private void setupSimilarProducts() {
        if (item == null)
            return;
        ItemRepository itemRepository = new ItemRepository();
        UserRepository userRepository = new UserRepository();
        ItemViewModelFactory factory = new ItemViewModelFactory(userRepository, itemRepository);
        ItemViewModel itemViewModel = new ViewModelProvider(this, factory).get(ItemViewModel.class);
        itemViewModel.allItems.observe(this, allProducts -> {
            if (allProducts == null || allProducts.isEmpty())
                return;
            List<ItemModel> similar = new ArrayList<>();
            for (ItemModel p : allProducts) {
                if (p.getItemId().equals(item.getItemId()))
                    continue;
                if ((item.getCategory() != null && item.getCategory().equalsIgnoreCase(p.getCategory())) ||
                        (item.getBrand() != null && item.getBrand().equalsIgnoreCase(p.getBrand()))) {
                    similar.add(p);
                }
                if (similar.size() >= 10)
                    break;
            }
            AllItemAdapter adapter = new AllItemAdapter(similar);
            RecyclerView rv = findViewById(R.id.rvSimilar);
            int spanCount = 2;
            GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
            rv.setLayoutManager(layoutManager);
            rv.setAdapter(adapter);
            int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
            rv.addItemDecoration(new com.prm392.onlineshoesshop.activity.SpaceItemDecoration(spacing, spanCount));
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

    // Thêm tham số isBuyNow để phân biệt giữa Thêm vào giỏ hàng và Mua ngay
    private void showAddToCartBottomSheet() {
        showAddToCartBottomSheet(false);
    }

    private void showAddToCartBottomSheet(boolean isBuyNow) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_add_to_cart, null);
        bottomSheetDialog.setContentView(sheetView);
        // Bo tròn 2 góc trên cho bottom sheet
        sheetView.post(() -> {
            View parent = (View) sheetView.getParent();
            if (parent != null) {
                parent.setBackgroundResource(R.drawable.bottom_sheet_bg);
            }
        });

        ImageView imgProduct = sheetView.findViewById(R.id.imgProduct);
        TextView tvPrice = sheetView.findViewById(R.id.tvPrice);
        TextView tvStock = sheetView.findViewById(R.id.tvStock);
        RecyclerView sizeList = sheetView.findViewById(R.id.sizeList);
        TextView plusCartBtn = sheetView.findViewById(R.id.plusCartBtn);
        TextView minusCartBtn = sheetView.findViewById(R.id.minusCartBtn);
        TextView numberItemTxt = sheetView.findViewById(R.id.numberItemTxt);
        Button btnAddToCartConfirm = sheetView.findViewById(R.id.btnAddToCartConfirm);

        // Đổi text nút nếu là mua ngay
        if (isBuyNow) {
            btnAddToCartConfirm.setText("Mua ngay");
        } else {
            btnAddToCartConfirm.setText("Thêm vào Giỏ hàng");
        }

        // Load ảnh sản phẩm (ảnh đầu tiên)
        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(item.getPicUrl().get(0))
                    .placeholder(R.drawable.placeholder)
                    .into(imgProduct);
        } else {
            imgProduct.setImageResource(R.drawable.placeholder);
        }

        // Hiển thị giá
        try {
            String priceStr = String.valueOf(item.getPrice());
            double price = Double.parseDouble(priceStr);
            java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
            tvPrice.setText(String.format("₫%s", format.format(price)));
        } catch (Exception e) {
            tvPrice.setText(String.format("₫%s", item.getPrice()));
        }

        // Tính tổng kho (tổng số lượng các size còn hàng)
        int totalStock = 0;
        if (item.getSizeQuantityMap() != null) {
            for (int qty : item.getSizeQuantityMap().values()) {
                totalStock += qty;
            }
        }
        tvStock.setText("Kho: " + totalStock);

        // Setup size adapter cho modal
        SizeAdapter sheetSizeAdapter = new SizeAdapter(item.getSizeQuantityMap());
        sizeList.setAdapter(sheetSizeAdapter);
        sizeList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Số lượng mặc định
        final int[] quantity = { 1 };
        numberItemTxt.setText(String.valueOf(quantity[0]));

        // Ban đầu disable số lượng và nút xác nhận
        plusCartBtn.setEnabled(false);
        minusCartBtn.setEnabled(false);
        btnAddToCartConfirm.setEnabled(false);
        btnAddToCartConfirm.setAlpha(0.5f);

        // Lắng nghe chọn size để enable controls
        sheetSizeAdapter.setOnSizeSelectedListener(selectedSize -> {
            boolean enable = selectedSize != null;
            plusCartBtn.setEnabled(enable);
            minusCartBtn.setEnabled(enable);
            btnAddToCartConfirm.setEnabled(enable);
            btnAddToCartConfirm.setAlpha(enable ? 1f : 0.5f);
            // Reset số lượng về 1 khi chọn size mới
            quantity[0] = 1;
            numberItemTxt.setText("1");
        });

        plusCartBtn.setOnClickListener(v -> {
            String selectedSize = sheetSizeAdapter.getSelectedSize();
            if (selectedSize == null)
                return;
            int maxStock = 0;
            if (item.getSizeQuantityMap() != null && item.getSizeQuantityMap().containsKey(selectedSize)) {
                maxStock = item.getSizeQuantityMap().get(selectedSize);
            }
            if (quantity[0] < maxStock) {
                quantity[0]++;
                numberItemTxt.setText(String.valueOf(quantity[0]));
            }
        });
        minusCartBtn.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                numberItemTxt.setText(String.valueOf(quantity[0]));
            }
        });

        btnAddToCartConfirm.setOnClickListener(v -> {
            String selectedSize = sheetSizeAdapter.getSelectedSize();
            // Kiểm tra tồn kho size
            int maxStock = 0;
            if (item.getSizeQuantityMap() != null && item.getSizeQuantityMap().containsKey(selectedSize)) {
                maxStock = item.getSizeQuantityMap().get(selectedSize);
            }
            if (quantity[0] > maxStock) {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        "Không đủ số lượng yêu cầu!",
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.warning_orange));
                return;
            }
            item.setNumberInCart(quantity[0]);
            managementCart.insertItem(item, selectedSize, quantity[0]);
            if (isBuyNow) {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(this, CartActivity.class));
            } else {
                animateAddToCart(imgProduct, this::updateCartBadge);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void animateAddToCart(ImageView sourceImage, Runnable onEnd) {
        // Lấy vị trí icon giỏ hàng trên màn hình
        View cartIcon = binding.cartIconContainer;
        int[] cartLocation = new int[2];
        cartIcon.getLocationOnScreen(cartLocation);
        int cartX = cartLocation[0] + cartIcon.getWidth() / 2;
        int cartY = cartLocation[1] + cartIcon.getHeight() / 2;

        // Lấy vị trí ảnh sản phẩm trong modal trên màn hình
        int[] imgLocation = new int[2];
        sourceImage.getLocationOnScreen(imgLocation);
        int imgX = imgLocation[0];
        int imgY = imgLocation[1];

        // Tạo ImageView động
        ImageView flyingImage = new ImageView(this);
        flyingImage.setImageDrawable(sourceImage.getDrawable());
        int size = sourceImage.getWidth();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        flyingImage.setLayoutParams(params);

        // Thêm vào root view và đặt vị trí ban đầu tuyệt đối
        FrameLayout rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.addView(flyingImage);
        flyingImage.setX(imgX);
        flyingImage.setY(imgY);

        // Animate
        flyingImage.animate()
                .x(cartX - size / 2)
                .y(cartY - size / 2)
                .setDuration(1000)
                .withEndAction(() -> {
                    rootView.removeView(flyingImage);
                    // Hiệu ứng phình ra cho icon giỏ hàng
                    cartIcon.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(150)
                            .withEndAction(() -> cartIcon.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150)
                                    .withEndAction(() -> {
                                        if (onEnd != null)
                                            onEnd.run();
                                    })
                                    .start())
                            .start();
                })
                .start();
    }
}
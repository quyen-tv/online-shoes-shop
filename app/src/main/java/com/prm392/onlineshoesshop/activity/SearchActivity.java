package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.prm392.onlineshoesshop.R;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.databinding.ActivitySearchBinding;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.content.Intent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.prm392.onlineshoesshop.adapter.HistoryAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.prm392.onlineshoesshop.repository.ItemRepository;

public class SearchActivity extends AppCompatActivity {
    // UI
    private AutoCompleteTextView etSearch;
    private ImageView ivBack;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;

    // Data
    private List<String> historyList = new ArrayList<>();
    private ArrayAdapter<String> suggestionAdapter;
    private List<String> suggestionList = new ArrayList<>();

    private ActivitySearchBinding binding;

    private ActivityResultLauncher<Intent> searchResultLauncher;

    private ItemViewModel itemViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ActivityResultLauncher để nhận lại search_text khi quay lại
        searchResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String searchText = result.getData().getStringExtra("search_text");
                        if (searchText != null) {
                            etSearch.setText(searchText);
                            etSearch.setSelection(searchText.length());
                        }
                    }
                });

        // Init UI bằng binding
        ivBack = binding.ivBackSearch;
        etSearch = binding.etSearch;
        rvHistory = binding.rvHistory;
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyList, query -> {
            etSearch.setText(query);
            etSearch.setSelection(query.length());
            etSearch.dismissDropDown();
            Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
            intent.putExtra("search_query", query);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        rvHistory.setAdapter(historyAdapter);

        // Nếu có search_text từ intent thì set lại cho etSearch
        String searchText = getIntent().getStringExtra("search_text");
        if (searchText != null) {
            etSearch.setText(searchText);
            etSearch.setSelection(searchText.length());
        }

        // Lịch sử tìm kiếm mẫu (có thể lưu vào SharedPreferences nếu muốn)
        historyList = new ArrayList<>(Arrays.asList("Nike", "Adidas", "Puma"));
        updateHistory();

        // Khởi tạo suggestionList rỗng và adapter
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestionList);
        etSearch.setAdapter(suggestionAdapter);

        // Khởi tạo ViewModel để lấy danh sách sản phẩm thật
        UserRepository userRepository = new UserRepository();
        ItemRepository itemRepository = new ItemRepository();
        ItemViewModelFactory factory = new ItemViewModelFactory(userRepository, itemRepository);
        itemViewModel = new ViewModelProvider(this, factory).get(ItemViewModel.class);

        // Lắng nghe dữ liệu sản phẩm và cập nhật suggestionList (giới hạn 200 tên)
        itemViewModel.allItems.observe(this, items -> {
            suggestionList.clear();
            if (items != null) {
                int count = 0;
                for (com.prm392.onlineshoesshop.model.ItemModel item : items) {
                    if (item.getTitle() != null) {
                        suggestionList.add(item.getTitle());
                        count++;
                        if (count >= 200)
                            break;
                    }
                }
            }
            suggestionAdapter.notifyDataSetChanged();
        });

        // Gợi ý sản phẩm (nếu muốn giữ, có thể lấy từ ViewModel hoặc hardcode)
        // suggestionList = new ArrayList<>(Arrays.asList("Nike Air", "Adidas Ultra",
        // "Puma Classic")); // This line is removed
        // suggestionAdapter = new ArrayAdapter<>(this,
        // android.R.layout.simple_dropdown_item_1line, suggestionList); // This line is
        // removed
        etSearch.setThreshold(1);
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !suggestionList.isEmpty() && etSearch.getText().length() > 0) {
                etSearch.showDropDown();
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!suggestionList.isEmpty() && s.length() > 0) {
                    etSearch.showDropDown();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Search action
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    addToHistory(query);
                    Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                    intent.putExtra("search_query", query);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Không cần đồng bộ favorite hay kết quả ở đây nữa
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String searchText = intent.getStringExtra("search_text");
        if (searchText != null) {
            etSearch.setText(searchText);
            etSearch.setSelection(searchText.length());
        }
    }

    // Hiển thị lịch sử tìm kiếm
    private void updateHistory() {
        if (historyAdapter != null) {
            historyAdapter.setHistoryList(historyList);
        }
    }

    // Thêm vào lịch sử tìm kiếm (không trùng lặp, mới nhất lên đầu)
    private void addToHistory(String query) {
        historyList.remove(query);
        historyList.add(0, query);
        if (historyList.size() > 10)
            historyList = historyList.subList(0, 10);
        updateHistory();
    }
}
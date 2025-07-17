package com.prm392.onlineshoesshop.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import com.prm392.onlineshoesshop.viewmodel.ItemViewModel;
import com.prm392.onlineshoesshop.factory.ItemViewModelFactory;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;

import com.prm392.onlineshoesshop.adapter.ChatAdapter;
import com.prm392.onlineshoesshop.databinding.ActivityChatbotBinding;
import com.prm392.onlineshoesshop.R;

import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.function.Consumer;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.helper.TinyDB;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class ChatbotActivity extends AppCompatActivity {
    private ActivityChatbotBinding binding;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final String[] quickSuggestions = {
            "Shop có size 42 không?",
            "Có ưu đãi nào hôm nay không?",
            "Tư vấn chọn giày chạy bộ",
            "Tìm giày Adidas màu trắng",
            "Sản phẩm nào bán chạy nhất?",
            "Giày nào phù hợp cho nữ?",
            "Shop có giày màu đen không?",
            "Giày nào đang giảm giá?",
            "Giày nào có đệm êm?",
            "Shop có giày trẻ em không?"
    };

    private static final String GEMINI_API_KEY = "AIzaSyC2LuelA50oX-RR3ecEhpx2RMt-KSrhSXg";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
            + GEMINI_API_KEY;
    private final OkHttpClient httpClient = new OkHttpClient();

    private List<ItemModel> allProducts = new ArrayList<>();
    private TinyDB tinyDB;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tinyDB = new TinyDB(this);
        // Khôi phục lịch sử chat nếu có
        String chatJson = tinyDB.getString("ChatHistory");
        if (chatJson != null && !chatJson.isEmpty()) {
            Type type = new TypeToken<ArrayList<ChatMessage>>() {
            }.getType();
            ArrayList<ChatMessage> savedHistory = gson.fromJson(chatJson, type);
            if (savedHistory != null && !savedHistory.isEmpty()) {
                chatMessages.addAll(savedHistory);
            }
        }
        // Cuộn xuống dưới cùng nếu có lịch sử chat
        if (!chatMessages.isEmpty()) {
            binding.recyclerViewChat.post(() -> binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1));
        }

        // Khởi tạo ViewModel lấy sản phẩm
        ItemRepository itemRepository = new ItemRepository();
        UserRepository userRepository = new UserRepository();
        ItemViewModelFactory factory = new ItemViewModelFactory(userRepository, itemRepository);
        ItemViewModel itemViewModel = new ViewModelProvider(this, factory).get(ItemViewModel.class);
        itemViewModel.allItems.observe(this, items -> {
            if (items != null)
                allProducts = items;
        });

        chatAdapter = new ChatAdapter(chatMessages);
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewChat.setAdapter(chatAdapter);

        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.btnBack.setOnClickListener(v -> finish());

        setupQuickSuggestions();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] scrcoords = new int[2];
                v.getLocationOnScreen(scrcoords);
                float x = ev.getRawX() + v.getLeft() - scrcoords[0];
                float y = ev.getRawY() + v.getTop() - scrcoords[1];
                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    v.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setupQuickSuggestions() {
        LayoutInflater inflater = LayoutInflater.from(this);
        binding.layoutQuickSuggestions.removeAllViews();
        for (String suggestion : quickSuggestions) {
            TextView tv = (TextView) inflater.inflate(R.layout.item_quick_suggestion, binding.layoutQuickSuggestions,
                    false);
            tv.setText(suggestion);
            tv.setOnClickListener(v -> {
                binding.etMessage.setText(suggestion);
                binding.etMessage.setSelection(suggestion.length());
            });
            binding.layoutQuickSuggestions.addView(tv);
        }
    }

    // Tạo prompt sản phẩm chi tiết cho Gemini
    private String buildProductPrompt(List<ItemModel> products) {
        StringBuilder productInfo = new StringBuilder();
        for (ItemModel item : products) {
            if (item.getTitle() != null && item.getPrice() != null) {
                productInfo.append("- Tên: ").append(item.getTitle()).append("\n");
                if (item.getBrand() != null)
                    productInfo.append("  Hãng: ").append(item.getBrand()).append("\n");
                if (item.getCategory() != null)
                    productInfo.append("  Loại: ").append(item.getCategory()).append("\n");
                if (item.getColor() != null)
                    productInfo.append("  Màu sắc: ").append(item.getColor()).append("\n");
                productInfo.append("  Giá: ").append(String.format("%,.0f₫", item.getPrice())).append("\n");
                if (item.getDescription() != null)
                    productInfo.append("  Mô tả: ").append(item.getDescription()).append("\n");
                if (item.getFeatures() != null)
                    productInfo.append("  Ưu điểm: ").append(item.getFeatures()).append("\n");
                if (item.getTargetUser() != null)
                    productInfo.append("  Đối tượng: ").append(item.getTargetUser()).append("\n");
                if (item.getSold() != null)
                    productInfo.append("  Đã bán: ").append(item.getSold()).append("\n");
                if (item.getRating() != null)
                    productInfo.append("  Đánh giá: ").append(String.format("%.1f/5", item.getRating())).append("\n");
                if (item.getStockEntries() != null && !item.getStockEntries().isEmpty()) {
                    productInfo.append("  Size còn hàng: ");
                    for (ItemModel.StockEntry entry : item.getStockEntries()) {
                        if (entry.getQuantity() > 0) {
                            productInfo.append(entry.getSize()).append("(").append(entry.getQuantity())
                                    .append(" đôi), ");
                        }
                    }
                    productInfo.append("\n");
                }
                // Nếu có trường tag hoặc các trường khác, bổ sung ở đây
                productInfo.append("\n");
            }
        }
        return productInfo.toString();
    }

    // Tạo lịch sử hội thoại cho prompt
    private String buildChatHistory(List<ChatMessage> chatMessages) {
        StringBuilder history = new StringBuilder();
        for (ChatMessage msg : chatMessages) {
            if (msg.type == ChatMessage.Type.USER) {
                history.append("Khách: ").append(msg.message).append("\n");
            } else if (msg.type == ChatMessage.Type.BOT) {
                history.append("Sale: ").append(msg.message).append("\n");
            }
        }
        return history.toString();
    }

    // Lấy danh sách sản phẩm gợi ý dựa trên message và reply
    private List<ItemModel> getSuggestedProducts(String message, String botReply) {
        List<ItemModel> suggestions = new ArrayList<>();
        if (allProducts == null || allProducts.isEmpty() || botReply == null)
            return suggestions;
        String lowerReply = botReply.toLowerCase();
        for (ItemModel item : allProducts) {
            String title = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
            boolean inStock = false;
            if (item.getSizeQuantityMap() != null) {
                for (int qty : item.getSizeQuantityMap().values()) {
                    if (qty > 0) {
                        inStock = true;
                        break;
                    }
                }
            }
            if (title.length() > 0 && lowerReply.contains(title) && inStock) {
                suggestions.add(item);
            }
            if (suggestions.size() >= 3)
                break;
        }
        return suggestions;
    }

    private void saveChatHistory() {
        String chatJson = gson.toJson(chatMessages);
        tinyDB.putString("ChatHistory", chatJson);
    }

    private void sendMessage() {
        String message = binding.etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message))
            return;
        ChatMessage userMsg = new ChatMessage(message, ChatMessage.Type.USER, ChatMessage.Status.SENT);
        chatMessages.add(userMsg);
        int userMsgIndex = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(userMsgIndex);
        binding.recyclerViewChat.scrollToPosition(userMsgIndex);
        binding.etMessage.setText("");
        saveChatHistory();

        // Thêm typing indicator
        ChatMessage typingMsg = new ChatMessage(ChatMessage.Type.TYPING);
        chatMessages.add(typingMsg);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

        sendMessageToGemini(message, reply -> runOnUiThread(() -> {
            // Xóa typing indicator nếu có
            if (!chatMessages.isEmpty() && chatMessages.get(chatMessages.size() - 1).type == ChatMessage.Type.TYPING) {
                chatMessages.remove(chatMessages.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessages.size());
            }
            if (reply.startsWith("Lỗi") || reply.startsWith("Không có")) {
                userMsg.status = ChatMessage.Status.FAILED;
                chatAdapter.notifyItemChanged(userMsgIndex);
            } else {
                chatMessages.add(new ChatMessage(reply, ChatMessage.Type.BOT, ChatMessage.Status.NONE));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                // Gợi ý sản phẩm sau khi bot trả lời
                List<ItemModel> suggestions = getSuggestedProducts(message, reply);
                for (ItemModel product : suggestions) {
                    chatMessages.add(new ChatMessage(product));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                }
                if (!suggestions.isEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                }
                saveChatHistory();
            }
        }));
    }

    // Gửi message tới Gemini và nhận phản hồi qua callback
    private void sendMessageToGemini(String message, Consumer<String> callback) {
        String shopInfo = "Shop giày Solemate - 123 Lê Lợi, Q.1, TP.HCM. Hotline: 0909030111. Mở cửa 8h-21h mỗi ngày. "
                +
                "Chính sách: Đổi trả 7 ngày, bảo hành 12 tháng, freeship khi mua 2 đôi trở lên";
        String prompt = "Bạn là nhân viên bán hàng thân thiện, chuyên nghiệp của shop giày Solemate. " +
                "Nhiệm vụ của bạn là tư vấn cho khách hàng sản phẩm phù hợp nhất dựa trên nhu cầu họ hỏi. " +
                "Hãy trả lời tự nhiên, ngắn gọn, giống như hội thoại thật ngoài đời, tập trung vào lợi ích và sự phù hợp của sản phẩm với khách. "
                +
                "Chỉ gợi ý tối đa 2-3 sản phẩm phù hợp nhất, ưu tiên sản phẩm còn hàng, bán chạy, đánh giá cao. " +
                "Với mỗi sản phẩm, hãy nêu tên, giá, ưu điểm nổi bật hoặc lý do nên chọn (ví dụ: phù hợp nhu cầu, giá tốt, đang khuyến mãi, chất liệu, thương hiệu, v.v). "
                +
                "Nếu khách hỏi ngoài phạm vi shop, hãy từ chối lịch sự và hướng lại về sản phẩm. " +
                "Nếu không có sản phẩm phù hợp, hãy trả lời lịch sự và gợi ý khách thử từ khóa khác. " +
                "Không quảng cáo quá đà, không lặp lại thông tin, không thêm ký tự thừa. " +
                "Thông tin về shop: " + shopInfo + "\n" +
                "Dưới đây là thông tin sản phẩm của shop:\n" + buildProductPrompt(allProducts) +
                "\nLịch sử hội thoại:\n" + buildChatHistory(chatMessages) +
                "Khách: " + message + "\nSale:";

        try {
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", new JSONArray().put(content));

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.accept("Lỗi kết nối tới Gemini!");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.accept("Lỗi từ Gemini: " + response.message());
                        return;
                    }
                    String body = response.body().string();
                    String reply = parseGeminiResponse(body);
                    callback.accept(reply);
                }
            });
        } catch (Exception e) {
            callback.accept("Lỗi tạo request tới Gemini!");
        }
    }

    // Hàm parse JSON trả lời từ Gemini
    private String parseGeminiResponse(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray candidates = obj.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject first = candidates.getJSONObject(0);
                JSONObject content = first.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
        } catch (Exception e) {
            return "Lỗi phân tích dữ liệu trả về!";
        }
        return "Không có phản hồi từ Gemini!";
    }

    // Model tin nhắn
    public static class ChatMessage {
        public enum Type {
            USER, BOT, PRODUCT, TYPING
        }

        public enum Status {
            SENT, FAILED, NONE
        }

        public String message;
        public Type type;
        public Status status;
        public ItemModel product;

        public ChatMessage(String message, Type type) {
            this(message, type, Status.NONE);
        }

        public ChatMessage(String message, Type type, Status status) {
            this.message = message;
            this.type = type;
            this.status = status;
        }

        public ChatMessage(ItemModel product) {
            this.type = Type.PRODUCT;
            this.status = Status.NONE;
            this.product = product;
        }

        public ChatMessage(Type type) {
            this.type = type;
            this.status = Status.NONE;
        }
    }
}

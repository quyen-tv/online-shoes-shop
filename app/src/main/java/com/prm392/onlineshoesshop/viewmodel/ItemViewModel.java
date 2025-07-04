package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.repository.ItemRepository;
import com.prm392.onlineshoesshop.repository.UserRepository;

import java.util.List;

public class ItemViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public LiveData<List<ItemModel>> allItems;
    public LiveData<User> currentUserData;

    public ItemViewModel(UserRepository userRepository, ItemRepository itemRepository) {
        this.userRepository = userRepository;

        this.allItems = itemRepository.getAllItems();
        this.currentUserData = userRepository.getCurrentUserLiveData();

        itemRepository.getErrorMessage().observeForever(_errorMessage::setValue);
    }

    /**
     * Chuyển đổi trạng thái yêu thích của sản phẩm (thêm/xóa).
     * 
     * @param itemId ID của sản phẩm.
     */
    public void toggleFavorite(String itemId) {
        User currentUser = currentUserData.getValue();
        if (currentUser == null) {
            _errorMessage.setValue("Vui lòng đăng nhập để sử dụng chức năng yêu thích.");
            return;
        }

        if (currentUser.isFavorite(itemId)) {
            // Đã yêu thích, giờ xóa khỏi yêu thích
            userRepository.removeFavoriteItem(itemId)
                    .addOnFailureListener(e -> _errorMessage.setValue("Lỗi khi xóa khỏi yêu thích: " + e.getMessage()));
        } else {
            // Chưa yêu thích, giờ thêm vào yêu thích
            userRepository.addFavoriteItem(itemId)
                    .addOnFailureListener(e -> _errorMessage.setValue("Lỗi khi thêm vào yêu thích: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra xem sản phẩm có đang được yêu thích bởi người dùng hiện tại không.
     * 
     * @param itemId ID của sản phẩm.
     * @return LiveData<Boolean> cho biết trạng thái yêu thích.
     */
    public LiveData<Boolean> isItemFavorite(String itemId) {
        return Transformations.map(currentUserData, user -> user != null && user.isFavorite(itemId));
    }

    /**
     * Lấy danh sách ItemModel từ danh sách sản phẩm yêu thích của user hiện tại.
     * 
     * @param listener callback trả về List<ItemModel> yêu thích
     */
    public void fetchFavoriteItems(UserRepository.OnCompleteListener<List<ItemModel>> listener) {
        User user = currentUserData.getValue();
        if (user != null && user.getFavoriteItems() != null && !user.getFavoriteItems().isEmpty()) {
            userRepository.getFavoriteItemModels(user, listener);
        } else {
            listener.onComplete(new java.util.ArrayList<>());
        }
    }
}

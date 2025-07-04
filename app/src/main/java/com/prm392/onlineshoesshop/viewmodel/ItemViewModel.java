package com.prm392.onlineshoesshop.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.ItemUtils;

import java.util.Collections;
import java.util.List;

public class ItemViewModel extends ViewModel {
    private final UserRepository userRepository;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public LiveData<User> currentUserData;
    public LiveData<FirebaseUser> firebaseUserLiveData;

    public ItemViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.currentUserData = userRepository.getCurrentUserLiveData();
        this.firebaseUserLiveData = userRepository.getFirebaseUserLiveData();
    }

    /**
     * Chuyển đổi trạng thái yêu thích của sản phẩm (thêm/xóa).
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
    public boolean isFavor(String itemId) {
        String firebaseKey = ItemUtils.getFirebaseItemId(itemId); // => "item_123"
        User user = currentUserData.getValue();
        return user != null && user.getFavoriteItems().containsKey(firebaseKey);
    }


    /**
     * Kiểm tra xem sản phẩm có đang được yêu thích bởi người dùng hiện tại không.
     * @param itemId ID của sản phẩm.
     * @return LiveData<Boolean> cho biết trạng thái yêu thích.
     */
    public LiveData<Boolean> isItemFavorite(String itemId) {
        return Transformations.map(currentUserData, user -> user != null && user.isFavorite(itemId));
    }
    public void forceRefreshUserData() {
        userRepository.reloadCurrentUser(); // Tự định nghĩa trong repository
    }
    public void fetchFavoriteItems(UserRepository.OnCompleteListener<List<ItemModel>> listener) {
        User user = currentUserData.getValue();
        if (user == null) {
            Log.w("ItemViewModel", "fetchFavoriteItems: User is null, returning empty list.");
            listener.onComplete(Collections.emptyList());
            return;
        }

        Log.d("ItemViewModel", "fetchFavoriteItems: Fetching favorite items for user: " + user.getUid());
        userRepository.getFavoriteItemModels(user, items -> {
            Log.d("ItemViewModel", "fetchFavoriteItems: Received " + items.size() + " items.");
            listener.onComplete(items);
        });
    }


}

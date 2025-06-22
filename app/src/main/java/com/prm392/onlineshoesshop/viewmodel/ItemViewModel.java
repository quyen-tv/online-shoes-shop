package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.repository.UserRepository;

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

    /**
     * Kiểm tra xem sản phẩm có đang được yêu thích bởi người dùng hiện tại không.
     * @param itemId ID của sản phẩm.
     * @return LiveData<Boolean> cho biết trạng thái yêu thích.
     */
    public LiveData<Boolean> isItemFavorite(String itemId) {
        return Transformations.map(currentUserData, user -> user != null && user.isFavorite(itemId));
    }
}

package com.prm392.onlineshoesshop.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.repository.UserRepository;

public class AuthViewModel extends ViewModel {

    private final UserRepository userRepository;

    // MutableLiveData để theo dõi trạng thái loading của các thao tác xác thực
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    // MutableLiveData để chứa thông báo lỗi nếu có trong quá trình xác thực
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    // MutableLiveData để chỉ ra liệu một thao tác xác thực có thành công hay không
    private final MutableLiveData<Boolean> _authSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getAuthSuccess() {
        return _authSuccess;
    }

    // LiveData để theo dõi đối tượng FirebaseUser hiện tại từ UserRepository
    public LiveData<FirebaseUser> firebaseUserLiveData;

    // LiveData để theo dõi đối tượng User tùy chỉnh hiện tại từ UserRepository (từ Realtime Database)
    public LiveData<User> currentUserData;

    /**
     * Constructor của AuthViewModel.
     * Nhận một UserRepository làm dependency để thực hiện các thao tác xác thực.
     * Khởi tạo các LiveData để nhận dữ liệu từ UserRepository.
     *
     * @param userRepository Instance của UserRepository để tương tác với dữ liệu người dùng.
     */
    public AuthViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.firebaseUserLiveData = userRepository.getFirebaseUserLiveData();
        this.currentUserData = userRepository.getCurrentUserLiveData();
    }

    /**
     * Thực hiện thao tác đăng nhập bằng email và mật khẩu.
     * Cập nhật trạng thái loading, reset lỗi và trạng thái thành công trước khi gọi repository.
     * Sau khi thao tác hoàn tất, cập nhật lại trạng thái loading và kết quả (thành công/thất bại).
     *
     * @param email Email của người dùng.
     * @param password Mật khẩu của người dùng.
     */
    public void signIn(String email, String password) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _authSuccess.setValue(false);

        userRepository.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        _authSuccess.setValue(true);
                    } else {
                        _errorMessage.setValue(task.getException() != null ? task.getException().getMessage() : "Unknown error.");
                    }
                });
    }

    /**
     * Thực hiện thao tác đăng ký tài khoản mới bằng email và mật khẩu.
     * Cập nhật trạng thái loading, reset lỗi và trạng thái thành công trước khi gọi repository.
     * Sau khi thao tác hoàn tất, cập nhật lại trạng thái loading và kết quả (thành công/thất bại).
     *
     * @param email Email của tài khoản mới.
     * @param password Mật khẩu cho tài khoản mới.
     */
    public void signUp(String email, String password) {
        _isLoading.setValue(true); // Đặt trạng thái đang tải là true
        _errorMessage.setValue(null); // Xóa thông báo lỗi cũ
        _authSuccess.setValue(false); // Đặt trạng thái thành công là false

        userRepository.signUpWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    _isLoading.setValue(false); // Đặt trạng thái đang tải là false sau khi hoàn thành
                    if (task.isSuccessful()) {
                        _authSuccess.setValue(true); // Đăng ký thành công
                    } else {
                        // Cập nhật thông báo lỗi nếu đăng ký thất bại
                        _errorMessage.setValue(task.getException() != null ? task.getException().getMessage() : "Unknown error.");
                    }
                });
    }

    /**
     * Thực hiện thao tác đăng xuất người dùng.
     * Gọi phương thức signOut từ UserRepository và đặt lại trạng thái xác thực thành false.
     */
    public void logout() {
        userRepository.signOut();
        _authSuccess.setValue(false);
    }
}
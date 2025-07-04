package com.prm392.onlineshoesshop.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference usersRef;
    private static final String TAG = "UserRepository";

    // LiveData để theo dõi đối tượng người dùng hiện tại (từ Realtime Database)
    private final MutableLiveData<User> _currentUserLiveData = new MutableLiveData<>();
    public LiveData<User> getCurrentUserLiveData() {
        return _currentUserLiveData;
    }

    // LiveData để theo dõi đối tượng FirebaseUser (từ FirebaseAuth)
    private final MutableLiveData<FirebaseUser> _firebaseUserLiveData = new MutableLiveData<>();
    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return _firebaseUserLiveData;
    }


    /**
     * Constructor của UserRepository.
     * Khởi tạo FirebaseAuth và tham chiếu đến node "Users" trong Realtime Database.
     * Thêm một AuthStateListener để lắng nghe sự thay đổi trạng thái đăng nhập của người dùng.
     * Khi trạng thái thay đổi:
     * - Cập nhật _firebaseUserLiveData.
     * - Nếu có FirebaseUser, sẽ cố gắng lấy thông tin người dùng từ Realtime Database.
     * - Nếu người dùng chưa tồn tại trong DB, tạo một bản ghi User cơ bản và lưu vào DB.
     * - Nếu không có FirebaseUser (người dùng đã đăng xuất), đặt _currentUserLiveData về null.
     */
    public UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference("Users");

        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            _firebaseUserLiveData.postValue(firebaseUser);

            if (firebaseUser != null) {
                // Lắng nghe một lần sự kiện để lấy thông tin người dùng từ Realtime DB
                usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Người dùng đã tồn tại trong DB
                            User user = snapshot.getValue(User.class);
                            _currentUserLiveData.postValue(user);
                        } else {
                            // Người dùng chưa tồn tại trong DB
                            String phone = firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : null;

                            User basicUser = new User(
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail(),
                                    firebaseUser.getDisplayName(),
                                    firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                                    null,    // Address chưa có
                                    false,   // Không phải tài khoản Google
                                    phone    // ✅ Thêm phone number nếu có
                            );

                            _currentUserLiveData.postValue(basicUser);
                            saveUser(basicUser);
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch user profile from Realtime DB: " + error.getMessage());

                        // Lấy số điện thoại nếu có
                        String phone = firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : null;

                        // Tạo user cơ bản để đảm bảo LiveData không null
                        User basicUser = new User(
                                firebaseUser.getUid(),
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName(),
                                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                                null,    // Address
                                false,   // googleAccount
                                phone    // ✅ phoneNumber
                        );

                        _currentUserLiveData.postValue(basicUser);
                    }

                });
            } else {
                // Nếu không có FirebaseUser (đã đăng xuất), đặt currentUserLiveData về null
                _currentUserLiveData.postValue(null);
            }
        });
    }

    /**
     * Đăng nhập người dùng bằng email và mật khẩu.
     *
     * @param email Email của người dùng.
     * @param password Mật khẩu của người dùng.
     * @return Một Task<Void> biểu thị kết quả của thao tác đăng nhập.
     * Nếu thành công, Task sẽ hoàn thành. Nếu thất bại, Task sẽ chứa Exception.
     */
    public Task<Void> signInWithEmailAndPassword(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return null;
                });
    }

    /**
     * Đăng ký người dùng mới bằng email và mật khẩu.
     * Sau khi đăng ký thành công trên Firebase Authentication,
     * một bản ghi User cơ bản cũng sẽ được lưu vào Realtime Database.
     *
     * @param email Email của người dùng mới.
     * @param password Mật khẩu của người dùng mới.
     * @return Một Task<Void> biểu thị kết quả của thao tác đăng ký và lưu người dùng vào DB.
     * Nếu thành công, Task sẽ hoàn thành. Nếu thất bại, Task sẽ chứa Exception.
     */
    public Task<Void> signUpWithEmailAndPassword(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String name = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
                            String phone = firebaseUser.getPhoneNumber() != null ? firebaseUser.getPhoneNumber() : null;

                            User newUser = new User(
                                    firebaseUser.getUid(),
                                    email,
                                    name,
                                    null,     // profileImageUrl
                                    null,     // address
                                    false,    // googleAccount
                                    phone     // ✅ thêm phoneNumber
                            );
                            return saveUser(newUser);
                        }
                    }
                    throw task.getException(); // báo lỗi nếu đăng ký thất bại
                });
    }


    /**
     * Đăng xuất người dùng hiện tại khỏi Firebase Authentication.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Lấy đối tượng FirebaseUser hiện tại.
     *
     * @return Đối tượng FirebaseUser nếu có người dùng đăng nhập, ngược lại là null.
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Lưu hoặc cập nhật thông tin người dùng vào Realtime Database.
     * Dữ liệu sẽ được lưu dưới node "Users" với key là UID của người dùng.
     *
     * @param user Đối tượng User cần lưu.
     * @return Một Task<Void> biểu thị kết quả của thao tác lưu.
     * Trả về Task lỗi nếu User hoặc UID không hợp lệ.
     */
    public Task<Void> saveUser(User user) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("User or User ID is invalid."));
        }
        return usersRef.child(user.getUid()).setValue(user);
    }

    /**
     * Cập nhật một phần thông tin của người dùng trong Realtime Database.
     *
     * @param userId ID của người dùng cần cập nhật.
     * @param updates Một Map chứa các cặp key-value (tên trường và giá trị mới) để cập nhật.
     * @return Một Task<Void> biểu thị kết quả của thao tác cập nhật.
     * Trả về Task lỗi nếu User ID hoặc updates không hợp lệ.
     */
    public Task<Void> updateUserProfile(String userId, Map<String, Object> updates) {
        if (userId == null || userId.isEmpty() || updates == null || updates.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("User ID or updates are invalid."));
        }
        return usersRef.child(userId).updateChildren(updates);
    }

    /**
     * Lấy thông tin người dùng từ Realtime Database dựa trên User ID.
     *
     * @param userId ID của người dùng cần lấy.
     * @return Một Task<User> chứa đối tượng User nếu tìm thấy, hoặc null nếu không tìm thấy.
     * Nếu có lỗi trong quá trình lấy dữ liệu, Task sẽ chứa Exception.
     * Trả về Task lỗi nếu User ID không hợp lệ.
     */
    public Task<User> getUserById(String userId) {
        if (userId == null || userId.isEmpty()) {
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("User ID cannot be null or empty."));
        }
        return usersRef.child(userId).get().continueWith(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    return snapshot.getValue(User.class);
                } else {
                    return null;
                }
            } else {
                throw task.getException();
            }
        });
    }

    /**
     * Thêm sản phẩm vào danh sách yêu thích của người dùng hiện tại.
     * @param itemId ID của sản phẩm cần thêm.
     * @return Task<Void> để lắng nghe sự hoàn thành của thao tác.
     */
    public Task<Void> addFavoriteItem(String itemId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            return Tasks.forException(new IllegalStateException("User not logged in."));
        }
        if (itemId == null || itemId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Product ID cannot be null or empty."));
        }

        String userId = firebaseUser.getUid();
        String firebaseKey = ItemUtils.getFirebaseItemId(itemId);
        DatabaseReference userFavoriteRef = usersRef.child(userId).child("favoriteItems").child(firebaseKey);

        User currentUser = _currentUserLiveData.getValue();
        if (currentUser != null) {
            User updatedUser = new User(currentUser);
            updatedUser.getFavoriteItems().put(firebaseKey, true);
            _currentUserLiveData.postValue(updatedUser);
        }

        return userFavoriteRef.setValue(true)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add favorite product to Firebase: " + e.getMessage());
                    User failedUser = _currentUserLiveData.getValue();
                    if (failedUser != null) {
                        User revertedUser = new User(failedUser);
                        revertedUser.getFavoriteItems().remove(firebaseKey);
                        _currentUserLiveData.postValue(revertedUser);
                    }
                });
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích của người dùng hiện tại.
     * @param itemId ID của sản phẩm cần xóa.
     * @return Task<Void> để lắng nghe sự hoàn thành của thao tác.
     */
    public Task<Void> removeFavoriteItem(String itemId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            return Tasks.forException(new IllegalStateException("User not logged in."));
        }
        if (itemId == null || itemId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Product ID cannot be null or empty."));
        }

        String userId = firebaseUser.getUid();
        String firebaseKey = ItemUtils.getFirebaseItemId(itemId);
        DatabaseReference userFavoriteRef = usersRef.child(userId).child("favoriteItems").child(firebaseKey);

        User currentUser = _currentUserLiveData.getValue();
        if (currentUser != null) {
            User updatedUser = new User(currentUser);
            updatedUser.getFavoriteItems().remove(firebaseKey);
            _currentUserLiveData.postValue(updatedUser);
        }

        return userFavoriteRef.setValue(null)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove favorite product from Firebase: " + e.getMessage());
                    User failedUser = _currentUserLiveData.getValue();
                    if (failedUser != null) {
                        User revertedUser = new User(failedUser);
                        revertedUser.getFavoriteItems().put(firebaseKey, true);
                        _currentUserLiveData.postValue(revertedUser);
                    }
                });
    }

    /**
     * Lấy ID sản phẩm yêu thích tại vị trí cụ thể.
     * @param itemId Vị trí trong danh sách yêu thích (bắt đầu từ 0).
     * @return ID của sản phẩm yêu thích nếu hợp lệ, ngược lại trả về null.
     */
    public boolean isFavorite(String itemId) {
        User currentUser = _currentUserLiveData.getValue();
        if (currentUser == null || currentUser.getFavoriteItems() == null) {
            return false;
        }

        Map<String, Boolean> favoriteMap = currentUser.getFavoriteItems();
        return favoriteMap.containsKey(itemId);
    }

    public void reloadCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        _currentUserLiveData.postValue(user);  // 🔁 Trigger cập nhật LiveData
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to reload current user: " + error.getMessage());
                }
            });
        }
    }
    public void getFavoriteItemModels(User user, OnCompleteListener<List<ItemModel>> listener) {
        List<String> itemIds = new ArrayList<>(user.getFavoriteItems().keySet());
        List<ItemModel> result = new ArrayList<>();

        if (itemIds.isEmpty()) {
            listener.onComplete(result);
            return;
        }

        final int[] completedCount = {0}; // Đếm tổng số phản hồi (thành công + thất bại)

        for (String firebaseKey : itemIds) {
            String itemId = ItemUtils.getItemIdFromFirebaseKey(firebaseKey); // dùng HÀM LOẠI "item_"
            Log.d("getFavoriteItemModels", "Fetching itemId: " + itemId + " (from key: " + firebaseKey + ")");

            FirebaseDatabase.getInstance().getReference("Items")
                    .child(itemId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            ItemModel item = snapshot.getValue(ItemModel.class);
                            if (item != null) {
                                Log.d("getFavoriteItemModels", "Fetched item: " + itemId + " - " + item.getTitle());
                                item.setItemId(itemId);
                                result.add(item);
                            } else {
                                Log.w("getFavoriteItemModels", "Snapshot is not null but item is null for ID: " + itemId);
                            }
                        } else {
                            Log.w("getFavoriteItemModels", "No data found for itemId: " + itemId);
                        }

                        completedCount[0]++;
                        if (completedCount[0] == itemIds.size()) {
                            listener.onComplete(result);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("getFavoriteItemModels", "Failed to fetch itemId: " + itemId, e);
                        completedCount[0]++;
                        if (completedCount[0] == itemIds.size()) {
                            listener.onComplete(result);
                        }
                    });
        }

    }

    public interface OnCompleteListener<T> {
        void onComplete(T data);
    }


}
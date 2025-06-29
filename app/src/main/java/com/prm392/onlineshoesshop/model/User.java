package com.prm392.onlineshoesshop.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.prm392.onlineshoesshop.utils.ItemUtils;

import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {
    private String uid;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private Address address;
    private boolean googleAccount;
    private Map<String, Boolean> favoriteItems;
    private String phoneNumber; // ✅ Thêm mới

    public User() {
    }

    public User(User user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.fullName = user.getFullName();
        this.profileImageUrl = user.getProfileImageUrl();
        this.address = (user.getAddress() != null) ? new Address(user.getAddress()) : null;
        this.googleAccount = user.isGoogleAccount();
        this.favoriteItems = (user.getFavoriteItems() != null) ? new HashMap<>(user.getFavoriteItems()) : new HashMap<>();
        this.phoneNumber = user.getPhoneNumber(); // ✅ Copy phoneNumber

    }

    public User(String uid, String email, String fullName, String profileImageUrl, Address address, boolean googleAccount, String phoneNumber) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.address = address;
        this.googleAccount = googleAccount;
        this.favoriteItems = new HashMap<>();
        this.phoneNumber = phoneNumber; // ✅ Gán phoneNumber

    }

    protected User(Parcel in) {
        uid = in.readString();
        email = in.readString();
        fullName = in.readString();
        profileImageUrl = in.readString();
        address = in.readParcelable(Address.class.getClassLoader());
        googleAccount = in.readByte() != 0;
        favoriteItems = new HashMap<>();
        in.readMap(favoriteItems, String.class.getClassLoader());
        phoneNumber = in.readString(); // ✅ Đọc phoneNumber

    }

    public static final Creator<User> CREATOR = new Creator<>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    // ✅ Getter và Setter cho phoneNumber
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Address getAddress() {
        if (address == null) {
            address = new Address();
            address.setCity(new Address.City(-1, ""));
            address.setDistrict(new Address.District(-1, ""));
            address.setWard(new Address.Ward(-1, ""));
            address.setStreet("");
            address.setCountry("");
        }
        return address;
    }


    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(boolean googleAccount) {
        this.googleAccount = googleAccount;
    }

    public Map<String, Boolean> getFavoriteItems() {
        if (favoriteItems == null) {
            favoriteItems = new HashMap<>();
        }
        return favoriteItems;
    }

    public void setFavoriteItems(Map<String, Boolean> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }

    // Phương thức tiện ích để kiểm tra sản phẩm có trong danh sách yêu thích không
    public boolean isFavorite(String itemId) {
        String firebaseKey = ItemUtils.getFirebaseItemId(itemId);
        return favoriteItems != null && favoriteItems.containsKey(firebaseKey) && Boolean.TRUE.equals(favoriteItems.get(firebaseKey));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(email);
        dest.writeString(fullName);
        dest.writeString(profileImageUrl);
        dest.writeParcelable(address, flags);
        dest.writeByte((byte) (googleAccount ? 1 : 0));
        dest.writeMap(favoriteItems);
        dest.writeString(phoneNumber); // ✅ Ghi phoneNumber

    }
}

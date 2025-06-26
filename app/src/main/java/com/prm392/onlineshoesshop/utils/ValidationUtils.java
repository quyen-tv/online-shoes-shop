package com.prm392.onlineshoesshop.utils;

import android.util.Patterns;

public class ValidationUtils {

    // Độ dài tối thiểu của mật khẩu
    public static final int MIN_PASSWORD_LENGTH = 6;

    // Phương thức kiểm tra trường có chứa khoảng trắng không
    public static boolean containsWhitespace(String text) {
        return text != null && text.contains(" ");
    }

    // Phương thức kiểm tra trường có trống không
    public static boolean isFieldEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // Phương thức kiểm tra email có hợp lệ không
    public static boolean isValidEmail(String email) {
        return !isFieldEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Phương thức kiểm tra độ mạnh của mật khẩu
    public static boolean isValidPassword(String password) {
        return !isFieldEmpty(password) && password.length() >= MIN_PASSWORD_LENGTH;
    }

    // Phương thức kiểm tra hai mật khẩu có khớp nhau không
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return !isFieldEmpty(password) && password.equals(confirmPassword);
    }
    public static boolean isValidPhoneNumber(String phone) {
        // Kiểm tra theo định dạng Việt Nam: bắt đầu bằng 0, có 10 chữ số
        return phone.matches("^0\\d{9}$");
    }

}

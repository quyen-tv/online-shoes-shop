package com.prm392.onlineshoesshop.utils;

public class UserUtils {

    /**
     * Tách phần tên người dùng từ địa chỉ email (phần trước ký tự '@').
     * Ví dụ: "john.doe@example.com" sẽ trả về "john.doe"
     *
     * @param email Địa chỉ email đầy đủ.
     * @return Tên người dùng được trích xuất, hoặc một chuỗi rỗng nếu email không hợp lệ hoặc rỗng.
     */
    public static String extractNameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "";
    }
}

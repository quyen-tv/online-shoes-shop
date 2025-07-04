package com.prm392.onlineshoesshop.utils;

public class ItemUtils {
    public static String getItemIdFromFirebaseKey(String firebaseKey) {
        return firebaseKey.replace("item_", "");
    }
    public static String getFirebaseItemId(String originalItemId) {
        return "item_" + originalItemId;
    }
}

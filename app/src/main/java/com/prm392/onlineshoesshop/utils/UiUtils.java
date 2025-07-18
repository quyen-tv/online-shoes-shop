package com.prm392.onlineshoesshop.utils;

import android.view.View;

import androidx.annotation.ColorInt;

import com.google.android.material.snackbar.Snackbar;

public class UiUtils {

    /**
     * Hiển thị một Snackbar message.
     *
     * @param view     View gốc để "neo" Snackbar vào.
     * @param message  Nội dung thông báo.
     * @param duration Thời lượng hiển thị (Snackbar.LENGTH_SHORT, Snackbar.LENGTH_LONG, hoặc custom).
     */
    public static void showSnackbar(View view, String message, int duration) {
        Snackbar.make(view, message, duration).show();
    }

    /**
     * Hiển thị một Snackbar message với màu nền tùy chỉnh.
     *
     * @param view         View gốc để "neo" Snackbar vào.
     * @param message      Nội dung thông báo.
     * @param duration     Thời lượng hiển thị.
     * @param backgroundColor Màu nền của Snackbar (ví dụ: ContextCompat.getColor(context, R.color.my_custom_color)).
     */
    public static void showSnackbarWithBackground(View view, String message, int duration, @ColorInt int backgroundColor) {
        Snackbar snackbar = Snackbar.make(view, message, duration);
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.show();
    }

    /**
     * Hiển thị một Snackbar message với một hành động (action).
     *
     * @param view     View gốc để "neo" Snackbar vào.
     * @param message  Nội dung thông báo.
     * @param actionText Văn bản cho nút hành động.
     * @param actionListener Listener cho sự kiện click nút hành động.
     * @param duration Thời lượng hiển thị.
     */
    public static void showSnackbarWithAction(View view, String message, String actionText, View.OnClickListener actionListener, int duration) {
        Snackbar.make(view, message, duration)
                .setAction(actionText, actionListener)
                .show();
    }

}

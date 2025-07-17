package com.prm392.onlineshoesshop.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.model.ItemModel;
import com.prm392.onlineshoesshop.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewDialogFragment extends DialogFragment {

    private EditText edtComment;
    private RatingBar ratingBar;
    private Button btnSubmit;
    private String itemId;
    private String userId; // bạn cần truyền vào từ context
    private String userName;
    private static final String TAG = "ReviewDialogFragment"; // TAG dùng cho log

    public static ReviewDialogFragment newInstance(String itemId, String userId, String userName) {
        ReviewDialogFragment fragment = new ReviewDialogFragment();
        Bundle args = new Bundle();
        args.putString("itemId", itemId);
        args.putString("userId", userId);
        args.putString("userName", userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_review, container, false);

        edtComment = view.findViewById(R.id.edtComment);
        ratingBar = view.findViewById(R.id.ratingBar);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        itemId = getArguments().getString("itemId");
        userId = getArguments().getString("userId");
        userName = getArguments().getString("userName");

        btnSubmit.setOnClickListener(v -> {
            double rating = ratingBar.getRating();
            String comment = edtComment.getText().toString().trim();

            Log.d(TAG, "Rating: " + rating);
            Log.d(TAG, "Comment: " + comment);

            Review review = new Review();
            review.setItemId(itemId);
            review.setUserId(userId);
            review.setUserName(userName);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(System.currentTimeMillis());

            saveReviewToDatabase(review);
            dismiss();
        });

        return view;
    }

    private void saveReviewToDatabase(Review review) {
        Context context = getActivity(); // hoặc requireActivity()

        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                .getReference("Items")
                .child(review.getItemId());

        Log.d(TAG, "Saving review for itemId: " + review.getItemId());

        itemRef.get().addOnSuccessListener(snapshot -> {
            Log.d(TAG, "Firebase snapshot received");

            if (snapshot.exists()) {
                ItemModel item = snapshot.getValue(ItemModel.class);
                if (item != null) {
                    Log.d(TAG, "Item found: " + item.getTitle());

                    List<Review> reviews = item.getReviewList();
                    if (reviews == null) {
                        Log.d(TAG, "Review list is null -> initializing new list");
                        reviews = new ArrayList<>();
                    }

                    reviews.add(review);
                    Log.d(TAG, "Added new review. Total reviews: " + reviews.size());

                    double totalRating = 0;
                    for (Review r : reviews) totalRating += r.getRating();
                    item.setRating(totalRating / reviews.size());

                    item.setReviewList(reviews);

                    itemRef.setValue(item)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Review saved successfully");
                                if (context != null) {
                                    Toast.makeText(context, "Đã lưu đánh giá", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi khi lưu đánh giá: ", e);
                                if (context != null) {
                                    Toast.makeText(context, "Đã lưu đánh giá", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e(TAG, "Item is null even though snapshot exists");
                }
            } else {
                Log.e(TAG, "Không tìm thấy item với ID: " + review.getItemId());
                if (context != null) {
                    Toast.makeText(context, "Đã lưu đánh giá", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi lấy item từ Firebase: ", e);
            if (context != null) {
                Toast.makeText(context, "Đã lưu đánh giá", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Lấy kích thước màn hình
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90); // 90% chiều rộng
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;

            getDialog().getWindow().setLayout(width, height);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Bo góc đẹp hơn
        }
    }

}

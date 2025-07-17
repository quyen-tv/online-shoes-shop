package com.prm392.onlineshoesshop.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {
    private final DatabaseReference itemsRef;

    public ReviewRepository() {
        itemsRef = FirebaseDatabase.getInstance().getReference("Items");
    }

    public LiveData<List<Review>> getReviewsByItemId(String itemId) {
        MutableLiveData<List<Review>> reviewsLiveData = new MutableLiveData<>();
        itemsRef.child(itemId).child("reviewList")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Review> reviews = new ArrayList<>();
                        for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                            Review review = reviewSnapshot.getValue(Review.class);
                            if (review != null) {
                                // Nếu là mảng thì key là index (0,1,2,...)
                                review.setReviewId(reviewSnapshot.getKey());
                                reviews.add(review);
                            }
                        }
                        // Sắp xếp mới nhất lên đầu
                        reviews.sort((r1, r2) -> Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                        reviewsLiveData.setValue(reviews);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        reviewsLiveData.setValue(new ArrayList<>());
                    }
                });
        return reviewsLiveData;
    }

    public void addReview(String itemId, Review review) {
        String reviewId = itemsRef.child(itemId).child("reviewList").push().getKey();
        if (reviewId != null) {
            review.setReviewId(reviewId);
            itemsRef.child(itemId).child("reviewList").child(reviewId).setValue(review);
        }
    }

    public void updateReview(String itemId, Review review) {
        if (review.getReviewId() != null) {
            itemsRef.child(itemId).child("reviewList").child(review.getReviewId()).setValue(review);
        }
    }

    public void deleteReview(String itemId, String reviewId) {
        itemsRef.child(itemId).child("reviewList").child(reviewId).removeValue();
    }
}
package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prm392.onlineshoesshop.model.User;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                fetchUserData(currentUser.getUid(), new OnUserDataFetchListener() {
                    @Override
                    public void onSuccess(User user) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtra("user_data", user));
                    }
                    @Override
                    public void onFailure(Exception e) {
                        startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    }
                });
            } else {
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
            }
            finish();
    }

    public interface OnUserDataFetchListener {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public void fetchUserData(String userId, @NonNull OnUserDataFetchListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onFailure(new IllegalArgumentException("User ID cannot be null or empty."));
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    listener.onSuccess(user);
                } else {
                    listener.onFailure(new Exception("User with ID " + userId + " not found in Realtime Database."));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toException());
            }
        });
    }
}
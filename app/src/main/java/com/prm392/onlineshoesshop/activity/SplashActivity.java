package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

public class SplashActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserRepository userRepository = new UserRepository();
        AuthViewModelFactory factory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        authViewModel.firebaseUserLiveData.observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Log.d(TAG, "Firebase user detected: " + firebaseUser.getUid());
                authViewModel.currentUserData.observe(this, customUser -> {
                    if (customUser != null) {
                        Log.d(TAG, "Custom user data loaded for: " + customUser.getEmail());
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Log.w(TAG, "Firebase user exists, but custom user data is null. Navigating to IntroActivity.");
                        startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                        finish();
                    }
                });
            } else {
                Log.d(TAG, "No Firebase user detected. Navigating to IntroActivity.");
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                finish();
            }
        });
    }
}
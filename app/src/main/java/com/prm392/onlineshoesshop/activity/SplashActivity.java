package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prm392.onlineshoesshop.factory.AuthViewModelFactory;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class SplashActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private ProgressBar progressBar;

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showNoNetworkDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối")
                .setMessage("Không có kết nối mạng. Vui lòng kiểm tra lại kết nối Internet của bạn.")
                .setCancelable(false)
                .setPositiveButton("Thử lại", (dialog, which) -> recreate())
                .setNegativeButton("Thoát", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isNetworkAvailable()) {
            showNoNetworkDialog();
            return;
        }

        UserRepository userRepository = new UserRepository();
        AuthViewModelFactory factory = new AuthViewModelFactory(userRepository);
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);

        // Thêm ProgressBar động
        progressBar = new ProgressBar(this);
        addContentView(progressBar, new android.widget.FrameLayout.LayoutParams(
                150, 150, android.view.Gravity.CENTER));
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        // Timeout: Nếu sau 5 giây không có dữ liệu thì kiểm tra mạng rồi mới chuyển
        timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutRunnable = () -> {
            if (!isFinishing()) {
                if (!isNetworkAvailable()) {
                    progressBar.setVisibility(View.GONE);
                    showNoNetworkDialog();
                    return;
                }
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                finish();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 5000);

        authViewModel.firebaseUserLiveData.observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                authViewModel.currentUserData.observe(this, customUser -> {
                    if (customUser != null) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        if (!isNetworkAvailable()) {
                            progressBar.setVisibility(View.GONE);
                            showNoNetworkDialog();
                            return;
                        }
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    } else {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        if (!isNetworkAvailable()) {
                            progressBar.setVisibility(View.GONE);
                            showNoNetworkDialog();
                            return;
                        }
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                        finish();
                    }
                });
            } else {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                if (!isNetworkAvailable()) {
                    progressBar.setVisibility(View.GONE);
                    showNoNetworkDialog();
                    return;
                }
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                finish();
            }
        });
    }
}
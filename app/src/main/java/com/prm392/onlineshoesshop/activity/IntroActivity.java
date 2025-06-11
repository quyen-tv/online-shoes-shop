package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.prm392.onlineshoesshop.databinding.ActivityIntroBinding;

public class IntroActivity extends AppCompatActivity {

    private ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvIntroSignUp.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity.this, SignUpActivity.class));
        });

        binding.btnStart.setOnClickListener(v -> {
            startActivity(new Intent(IntroActivity.this, SignInActivity.class));
        });
    }
}
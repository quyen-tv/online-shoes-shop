package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivitySignInBinding;
import com.prm392.onlineshoesshop.utils.GoogleAuthHandler;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        binding.btnSignIn.setOnClickListener(v -> {
            if (validateInputs()) {
                performSignIn();
            }
        });
        binding.btnSignUpWithGg.setOnClickListener(v -> {
            GoogleAuthHandler googleAuthHandler = new GoogleAuthHandler();
            googleAuthHandler.startGoogleSignIn(
                SignInActivity.this,
                false // isSignUp = false for sign-in
            );
        });
        binding.tvIntroSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
        setupTextWatchers();
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        View[] interactableViews = {
                binding.btnSignIn,
                binding.btnSignUpWithGg,
                binding.tvIntroSignUp,
                binding.tilEmail,
                binding.tilPassword,
                binding.tilConfirmPassword
        };

        Arrays.stream(interactableViews).forEach(view -> {
            view.setEnabled(!isLoading);
            view.setAlpha(isLoading ? 0.5f : 1.0f);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String email = binding.etEmail.getText().toString();
        String password = binding.etPassword.getText().toString();

        // 1. Kiểm tra Email
        if (ValidationUtils.isFieldEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else if (ValidationUtils.containsWhitespace(email)) {
            binding.tilEmail.setError(getString(R.string.error_no_spaces_email));
            isValid = false;
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        }

        // 2. Kiểm tra password
        if (ValidationUtils.isFieldEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else if (ValidationUtils.containsWhitespace(password)) {
            binding.tilPassword.setError(getString(R.string.error_no_spaces_password));
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(String.format(getString(R.string.error_password_too_short), ValidationUtils.MIN_PASSWORD_LENGTH));
            isValid = false;
        }


        return isValid;
    }

    private void setupTextWatchers() {
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


    }

    private void resetForm() {
        binding.etEmail.setText("");
        binding.etPassword.setText("");

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }

    private void performSignIn() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            resetForm();
                            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_successful), Snackbar.LENGTH_SHORT);
                        } else {
                            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.login_failed), Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        String errorMessage = getString(R.string.login_failed);
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        UiUtils.showSnackbar(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG);
                    }
                });

    }
}
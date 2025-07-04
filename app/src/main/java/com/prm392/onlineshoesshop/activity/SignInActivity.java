package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivitySignInBinding;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.GoogleAuthHandler;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserRepository userRepository = new UserRepository();
        authViewModel = new ViewModelProvider(
                this,
                new SignUpActivity.AuthViewModelFactory(userRepository))
                .get(AuthViewModel.class);

        setupObservers();
        setupListeners();
        setupTextWatchers();
    }

    /**
     * Thiết lập các sự kiện click cho các nút và TextView trong layout.
     * - btnSignIn: Xử lý sự kiện đăng nhập bằng email/password.
     * - btnSignUpWithGg: Xử lý sự kiện đăng ký/đăng nhập bằng Google.
     * - tvIntroSignUp: Chuyển hướng sang màn hình đăng ký (SignUpActivity).
     */
    private void setupListeners() {
        binding.btnSignIn.setOnClickListener(v -> {
            if (validateInputs()) {
                authViewModel.signIn(
                        binding.etEmail.getText().toString().trim(),
                        binding.etPassword.getText().toString().trim()
                );
            }
        });

        binding.btnSignUpWithGg.setOnClickListener(v -> {
            GoogleAuthHandler googleAuthHandler = new GoogleAuthHandler();
            googleAuthHandler.startGoogleSignIn(
                    SignInActivity.this,
                    false
            );
        });

        binding.tvIntroSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }

    /**
     * Thiết lập các Observer để theo dõi sự thay đổi của LiveData từ AuthViewModel.
     * - isLoading: Hiển thị/ẩn ProgressBar và vô hiệu hóa/kích hoạt các phần tử UI.
     * - errorMessage: Hiển thị Snackbar với thông báo lỗi nếu có.
     * - authSuccess: Đặt lại form nếu đăng nhập/đăng ký thành công.
     * - currentUserData: Nếu đăng nhập thành công và có dữ liệu người dùng, chuyển đến MainActivity.
     */
    private void setupObservers() {
        authViewModel.getIsLoading().observe(this, this::showLoading);

        authViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        message,
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.error_red));
            }
        });

        authViewModel.getAuthSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                resetForm();
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    /**
     * Hiển thị hoặc ẩn ProgressBar và điều chỉnh trạng thái tương tác của các phần tử UI
     * dựa trên trạng thái loading.
     *
     * @param isLoading true nếu đang tải, false nếu không.
     */
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

    /**
     * Xác thực các trường nhập liệu (Email và Mật khẩu).
     * Hiển thị thông báo lỗi tương ứng dưới mỗi trường nếu dữ liệu không hợp lệ.
     *
     * @return true nếu tất cả các trường đều hợp lệ, ngược lại là false.
     */
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
        } else {
            binding.tilEmail.setError(null);
        }

        // 2. Kiểm tra mật khẩu
        if (ValidationUtils.isFieldEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else if (ValidationUtils.containsWhitespace(password)) {
            binding.tilPassword.setError(getString(R.string.error_no_spaces_password));
            isValid = false;
        } else if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError(String.format(getString(R.string.error_password_too_short), ValidationUtils.MIN_PASSWORD_LENGTH));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        return isValid;
    }

    /**
     * Thiết lập các TextWatcher cho trường Email và Mật khẩu.
     * Mỗi khi người dùng thay đổi văn bản, thông báo lỗi (nếu có) sẽ được xóa.
     */
    private void setupTextWatchers() {
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Đặt lại các trường nhập liệu Email và Mật khẩu về rỗng
     * và xóa mọi thông báo lỗi đang hiển thị.
     */
    private void resetForm() {
        binding.etEmail.setText("");
        binding.etPassword.setText("");

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }



}
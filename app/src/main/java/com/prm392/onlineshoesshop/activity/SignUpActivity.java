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
import com.prm392.onlineshoesshop.databinding.ActivitySignUpBinding;
import com.prm392.onlineshoesshop.repository.UserRepository;
import com.prm392.onlineshoesshop.utils.GoogleAuthHandler;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;
import com.prm392.onlineshoesshop.viewmodel.AuthViewModel;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserRepository userRepository = new UserRepository();
        authViewModel = new ViewModelProvider(
                this,
                new AuthViewModelFactory(userRepository))
                .get(AuthViewModel.class);

        setupObservers();
        setupListeners();
        setupTextWatchers();
    }

    /**
     * Thiết lập các sự kiện click cho các nút và TextView trong layout.
     * - btnSignUp: Xử lý sự kiện đăng ký bằng email/password.
     * - btnSignUpWithGg: Xử lý sự kiện đăng ký/đăng nhập bằng Google.
     * - tvIntroSignIn: Chuyển hướng sang màn hình đăng nhâp (SignInActivity).
     */
    private void setupListeners() {
        binding.btnSignUp.setOnClickListener(v -> {
            if (validateInputs()) {
                authViewModel.signUp(
                        binding.etEmail.getText().toString().trim(),
                        binding.etPassword.getText().toString().trim()
                );
            }
        });

        binding.btnSignUpWithGg.setOnClickListener(v -> {
            GoogleAuthHandler googleAuthHandler = new GoogleAuthHandler();
            googleAuthHandler.startGoogleSignIn(
                    SignUpActivity.this,
                    false
            );
        });

        binding.tvIntroSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
    }

    /**
     * Thiết lập các Observer để theo dõi sự thay đổi của LiveData từ AuthViewModel.
     * - isLoading: Hiển thị/ẩn ProgressBar và vô hiệu hóa/kích hoạt các phần tử UI.
     * - errorMessage: Hiển thị Snackbar với thông báo lỗi nếu có.
     * - authSuccess: Đặt lại form và thông báo thành công nếu đăng ký thành công.
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
                UiUtils.showSnackbarWithBackground(
                        binding.getRoot(),
                        getString(R.string.registration_successful_db_save),
                        Snackbar.LENGTH_LONG,
                        getResources().getColor(R.color.success_green));
            }
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
        String confirmPassword = binding.etConfirmPassword.getText().toString();

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

        // 3. Kiểm tra confirm password và password có match nhau không
        if (ValidationUtils.isFieldEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_field_empty));
            isValid = false;
        } else if (ValidationUtils.containsWhitespace(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_no_spaces_confirm_password));
            isValid = false;
        } else if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Đặt lại các trường nhập liệu Email và Mật khẩu về rỗng
     * và xóa mọi thông báo lỗi đang hiển thị.
     */
    private void resetForm() {
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        binding.etConfirmPassword.setText("");

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
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
                binding.btnSignUp,
                binding.btnSignUpWithGg,
                binding.tvIntroSignIn,
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
     * Thiết lập các TextWatcher cho trường Email và Mật khẩu.
     * Mỗi khi người dùng thay đổi văn bản, thông báo lỗi (nếu có) sẽ được xóa.
     */
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

        binding.etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Lớp ViewModelProvider.Factory tùy chỉnh.
     * Sử dụng để cung cấp một instance của AuthViewModel
     * với một UserRepository đã được khởi tạo.
     * Điều này cho phép AuthViewModel nhận các dependencies cần thiết thông qua constructor của nó.
     */
    public static class AuthViewModelFactory implements ViewModelProvider.Factory {
        private final UserRepository userRepository;

        public AuthViewModelFactory(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AuthViewModel.class)) {
                return (T) new AuthViewModel(userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
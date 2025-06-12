package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivitySignUpBinding;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInClient mGoogleSignInClient;
    
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.w(TAG, "Google sign in failed", e);
                        showLoading(false);
                        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.google_signin_failed), Snackbar.LENGTH_LONG);
                    }
                }
            }
    );    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnSignUp.setOnClickListener(v -> {
            if (validateInputs()) {
                performSignUp();
            }
        });
        
        binding.btnSignUpWithGg.setOnClickListener(v -> signUpWithGoogle());
        
        binding.tvIntroSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
        setupTextWatchers();
    }

    // Validate tất cả dữ liệu đầu vào
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

    // Xử lý nghiệp vụ đăng ký
    private void performSignUp() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            saveNewUserToDatabase(user);
                            resetForm();
                        } else {
                            UiUtils.showSnackbar(binding.getRoot(), getString(R.string.registration_failed_db_save), Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        String errorMessage = getString(R.string.registration_failed_db_save);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = getString(R.string.email_already_in_use);
                            binding.tilEmail.setError(errorMessage);
                        } else if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        UiUtils.showSnackbar(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG);
                    }
                });

    }    // Lưu user mới vào database
    private void saveNewUserToDatabase(@NonNull FirebaseUser firebaseUser) {
        String fullName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
        String profileImageUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";
        Address address = new Address("", "", "", "", "");
        User newUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), fullName, profileImageUrl, address);

        mDatabase.child("Users")
                .child(firebaseUser.getUid())
                .setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.registration_successful_db_save), Snackbar.LENGTH_SHORT);
                })
                .addOnFailureListener(e -> {
                    UiUtils.showSnackbar(binding.getRoot(), getString(R.string.registration_failed_db_save) + ": " + e.getMessage(), Snackbar.LENGTH_LONG);
                });
    }

    // Reset form
    private void resetForm() {
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        binding.etConfirmPassword.setText("");

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    // Hàm để hiển thị/ẩn ProgressBar và bật/tắt tương tác UI
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

    // Xóa lỗi của field khi người dùng nhập liệu
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
            }        });
    }

    private void signUpWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();                        if (user != null) {
                            // For Google Sign-Up, always save user data
                            saveNewUserToDatabase(user);
                            resetForm();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        String errorMessage = getString(R.string.google_signup_failed);
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        UiUtils.showSnackbar(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG);
                    }
                });
    }
}
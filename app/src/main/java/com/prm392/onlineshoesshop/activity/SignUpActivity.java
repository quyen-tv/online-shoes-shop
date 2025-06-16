package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import androidx.credentials.exceptions.GetCredentialException; // Add this line
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
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

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        binding.btnSignUp.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();

            if (validateInputs(email, password, confirmPassword)) {
                performSignUp(email, password);
            }
        });
        binding.btnSignUpWithGg.setOnClickListener(v -> {
            // Instantiate a Google sign-in request
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build();

            // Create the Credential Manager request
            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            // Use 'this' for context since this is an Activity
            CredentialManager credentialManager = CredentialManager.create(this);

            credentialManager.getCredentialAsync(
                    this, // Activity context
                    request, // The request you built above
                    null, // No cancellation signal
                    ContextCompat.getMainExecutor(this), // Main thread executor
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse result) {
                            handleGoogleSignIn(result);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            Snackbar snackbar = Snackbar.make(binding.getRoot(), "gg: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Dismiss", v1 -> {});
                            // Ensure the full message is visible
                            View snackbarView = snackbar.getView();
                            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setMaxLines(10); // or Integer.MAX_VALUE for unlimited lines
                            snackbar.show();
                        }
                    }
            );
        });
        binding.tvIntroSignIn.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
        setupTextWatchers();
    }

    // Validate tất cả dữ liệu đầu vào
    private boolean validateInputs(String email, String password, String confirmPassword) {
        boolean isValid = true;

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
    private void performSignUp(String email, String password) {
        performSignUp(email, password, false);

    }

    private void handleGoogleSignIn(GetCredentialResponse result) {
        // Extract the Google ID token from the credential
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                String idToken = googleIdTokenCredential.getIdToken();

                // Use the ID token to authenticate with Firebase
                signInWithGoogleToken(idToken);
            }
        }
    }

    private void signInWithGoogleToken(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        showLoading(true);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveNewUserToDatabase(user, true); // true indicates Google account
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        UiUtils.showSnackbar(binding.getRoot(), "Google authentication failed", Snackbar.LENGTH_LONG);
                    }
                });
    }


    private void performSignUp(String email, String password, boolean isGoogle) {

        showLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            saveNewUserToDatabase(user, isGoogle);
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

    }

    // Lưu user mới vào database
    private void saveNewUserToDatabase(@NonNull FirebaseUser firebaseUser) {
        saveNewUserToDatabase(firebaseUser, false);
    }

    private void saveNewUserToDatabase(@NonNull FirebaseUser firebaseUser, boolean isGoogle) {
        String fullName = "";
        String profileImageUrl = "";
        Address address = new Address("", "", "", "", "");
        User newUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), fullName, profileImageUrl, address, isGoogle);

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
            }
        });
    }
}
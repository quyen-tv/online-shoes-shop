package com.prm392.onlineshoesshop.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivitySignInBinding;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.utils.UiUtils;
import com.prm392.onlineshoesshop.utils.ValidationUtils;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    private ActivitySignInBinding binding;
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
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.btnSignIn.setOnClickListener(v -> {
            if (validateInputs()) {
                performSignIn();
            }
        });
        
        binding.btnSignUpWithGg.setOnClickListener(v -> signInWithGoogle());
        
        binding.tvIntroSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
        setupTextWatchers();
    }    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        View[] interactableViews = {
                binding.btnSignIn,
                binding.btnSignUpWithGg,
                binding.tvIntroSignUp,
                binding.tilEmail,
                binding.tilPassword
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
                    }                });

    }

    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check if this is a new user and save to database if needed
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                saveNewUserToDatabase(user);
                            }
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        UiUtils.showSnackbar(binding.getRoot(), getString(R.string.google_signin_failed), Snackbar.LENGTH_LONG);
                    }
                });
    }

    private void saveNewUserToDatabase(FirebaseUser firebaseUser) {
        String fullName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
        String profileImageUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "";
        Address address = new Address("", "", "", "", "");
        User newUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), fullName, profileImageUrl, address);

        mDatabase.child("Users")
                .child(firebaseUser.getUid())
                .setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created in database");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create user profile: " + e.getMessage());
                });
    }
}
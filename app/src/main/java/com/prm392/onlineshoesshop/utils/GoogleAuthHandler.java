package com.prm392.onlineshoesshop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prm392.onlineshoesshop.R;
import com.prm392.onlineshoesshop.databinding.ActivitySignInBinding;
import com.prm392.onlineshoesshop.databinding.ActivitySignUpBinding;
import com.prm392.onlineshoesshop.model.Address;
import com.prm392.onlineshoesshop.model.User;
import com.prm392.onlineshoesshop.activity.MainActivity;
import com.prm392.onlineshoesshop.utils.UiUtils;

public class GoogleAuthHandler {
    public static void startGoogleSignIn(Activity activity, View rootView, Object binding, boolean isSignUp) {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        CredentialManager credentialManager = CredentialManager.create(activity);
        credentialManager.getCredentialAsync(
                activity,
                request,
                null,
                ContextCompat.getMainExecutor(activity),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleSignIn(result, activity, rootView, binding, isSignUp);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Snackbar snackbar = Snackbar.make(rootView, "gg: " + e.getMessage(), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Dismiss", v1 -> {});
                        View snackbarView = snackbar.getView();
                        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setMaxLines(10);
                        snackbar.show();
                    }
                }
        );
    }

    private static void handleGoogleSignIn(GetCredentialResponse result, Activity activity, View rootView, Object binding, boolean isSignUp) {
        Credential credential = result.getCredential();
        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                String idToken = googleIdTokenCredential.getIdToken();
                signInWithGoogleToken(idToken, activity, rootView, binding, isSignUp);
            }
        }
    }

    private static void signInWithGoogleToken(String idToken, Activity activity, View rootView, Object binding, boolean isSignUp) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        if (binding instanceof ActivitySignInBinding) {
            ((ActivitySignInBinding) binding).progressBar.setVisibility(View.VISIBLE);
        } else if (binding instanceof ActivitySignUpBinding) {
            ((ActivitySignUpBinding) binding).progressBar.setVisibility(View.VISIBLE);
        }
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (binding instanceof ActivitySignInBinding) {
                        ((ActivitySignInBinding) binding).progressBar.setVisibility(View.GONE);
                    } else if (binding instanceof ActivitySignUpBinding) {
                        ((ActivitySignUpBinding) binding).progressBar.setVisibility(View.GONE);
                    }
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (isSignUp) {
                                saveNewUserToDatabase(user, rootView, true);
                            }
                            activity.startActivity(new Intent(activity, MainActivity.class));
                            activity.finish();
                        }
                    } else {
                        UiUtils.showSnackbar(rootView, "Google authentication failed", Snackbar.LENGTH_LONG);
                    }
                });
    }

    private static void saveNewUserToDatabase(@NonNull FirebaseUser firebaseUser, View rootView, boolean isGoogle) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String fullName = "";
        String profileImageUrl = "";
        Address address = new Address("", "", "", "", "");
        User newUser = new User(firebaseUser.getUid(), firebaseUser.getEmail(), fullName, profileImageUrl, address, isGoogle);
        mDatabase.child("Users")
                .child(firebaseUser.getUid())
                .setValue(newUser)
                .addOnSuccessListener(aVoid -> {
                    UiUtils.showSnackbar(rootView, "Registration successful", Snackbar.LENGTH_SHORT);
                })
                .addOnFailureListener(e -> {
                    UiUtils.showSnackbar(rootView, "Registration failed: " + e.getMessage(), Snackbar.LENGTH_LONG);
                });
    }
}

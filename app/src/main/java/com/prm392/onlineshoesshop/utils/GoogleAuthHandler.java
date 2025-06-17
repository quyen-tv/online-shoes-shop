package com.prm392.onlineshoesshop.utils;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
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

import java.util.concurrent.Executors;

public class GoogleAuthHandler {
    private FirebaseAuth mAuth;
    private Activity activity;
    private String TAG = "GoogleAuthHandler";
    public void startGoogleSignIn(Activity activity, boolean isSignUp) {
        mAuth = FirebaseAuth.getInstance();
        this.activity = activity;

        CredentialManager credentialManager = CredentialManager.create(activity);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.default_web_client_id))
                .build();

        GetSignInWithGoogleOption googleOption = new GetSignInWithGoogleOption
                .Builder(activity.getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build();

        credentialManager.getCredentialAsync(
                activity,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Extract credential from the result returned by Credential Manager
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Couldn't retrieve user's credentials: " + e.getLocalizedMessage());
                    }
                }
        );

    }

    private void handleSignIn(Credential credential) {
        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential
                && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            CustomCredential customCredential = (CustomCredential) credential;
            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken(), true);
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
        }
    }

    private void firebaseAuthWithGoogle(String idToken, boolean isSignUp) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser && user != null) {
                            // New user, save to database
                            saveNewUserToDatabase(user, activity.getWindow().getDecorView().getRootView(), true);
                        }
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
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

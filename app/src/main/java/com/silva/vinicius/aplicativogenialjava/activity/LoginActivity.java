package com.silva.vinicius.aplicativogenialjava.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.silva.vinicius.aplicativogenialjava.R;

import java.util.Arrays;
import java.util.Objects;

import com.silva.vinicius.aplicativogenialjava.config.FirebaseConfig;
import com.silva.vinicius.aplicativogenialjava.models.User;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "FBUI_AUTH";

    private FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseConfig.getAuthReference();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        //.setLogo(R.drawable.my_great_logo)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                updateUI(user);
            } else { // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return;
                }

                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {

                    Toast.makeText(LoginActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                Toast.makeText(LoginActivity.this, getString(R.string.fui_error_unknown), Toast.LENGTH_SHORT)
                        .show();

                Log.e(TAG, "Sign-in error: ", response.getError());
            }
            updateUI(null);
        }
    }

    private void updateUI(final FirebaseUser user) {

        if (user != null) {

            //Only saves if users does not exist already, based on FirebaseUser id.
            saveUserModel(user);

            Intent intent = new Intent(LoginActivity.this, AccountDetailsActivity.class);
            startActivity(intent);
        }
    }


    private void saveUserModel(final FirebaseUser user) {
        User user_model = new User(
                user.getUid(),
                user.getDisplayName(),
                user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null,
                null,
                null,
                false);

        user_model.save();
    }
}

package com.silva.vinicius.aplicativogenialjava.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public final class FirebaseConfig {

    private static DatabaseReference databaseReference;
    private static StorageReference storageReference;
    private static FirebaseAuth firebaseAuth;

    public static DatabaseReference getDatabaseReference() {

        if (databaseReference == null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            //Configs
            database.setPersistenceEnabled(true);
            databaseReference = database.getReference();
        }

        return databaseReference;
    }

    public static StorageReference getStorageReference() {
        if (storageReference == null) {
            storageReference = FirebaseStorage.getInstance().getReference();
        }

        return storageReference;
    }

    public static FirebaseAuth getAuthReference() {
        if (firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }

        return firebaseAuth;
    }

    /**
     * Returns the sign in provider that current user used to sign in.
     * @param currentUser the current user.
     * @return the sign in provider.
     */
    public static String getSignInProvider(FirebaseUser currentUser) {

        for (UserInfo user: currentUser.getProviderData()) {

            if (user.getProviderId().equals("google.com")) {
                return "google.com";
            } else if (user.getProviderId().equals("password")) {
                return "password";
            }
        }
        return null;
    }
}

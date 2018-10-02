package com.silva.vinicius.aplicativogenialjava.dao;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.silva.vinicius.aplicativogenialjava.config.FirebaseConfig;
import com.silva.vinicius.aplicativogenialjava.models.User;


public final class UserDAO {

    private static DatabaseReference userReference;

    private static DatabaseReference getUserReference() {
        if (userReference == null) {
            userReference = FirebaseConfig.getDatabaseReference().child("users");
        }
        return userReference;
    }

    private static void save(User user) {
        getUserReference().child(user.getId()).setValue(user);
    }

    public static void saveIfNotExists(final User user) {
        getUserReference().child("/"+user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {
                    save(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("SAVEUSERDATA", databaseError.getMessage());
            }
        });
    }

    /**
     * Deletes an user from Firebase Realtime Database.
     * @param user_id the id of user to be deleted.
     */
    public static void delete(final String user_id) {
        getUserReference().child("/"+user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DELETEUSERDATA", databaseError.getMessage());
            }
        });
    }

    public static void update(final User user) {
        //Retrieve user on Firebase Realtime Database.
        getUserReference().child("/"+user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("UPDATEUSERDATA", databaseError.getMessage());
            }
        });
    }
}

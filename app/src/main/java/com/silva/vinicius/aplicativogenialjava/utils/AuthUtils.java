package com.silva.vinicius.aplicativogenialjava.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.silva.vinicius.aplicativogenialjava.R;

import java.util.Objects;

public final class AuthUtils {

    /**
     * Updates the current user e-mail address on FirebaseAuth.
     * @param currentUser the current user.
     * @param new_email the new e-mail address.
     * @param parent the parent view.
     */
    public static void updateUserEmailAddress(final FirebaseUser currentUser, String new_email, final View parent) {
        currentUser.updateEmail(new_email)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //Send e-mail verification
                        sendEmailVerification(currentUser, parent.getContext());
                        Log.d("USER_EMAIL_UPDATED", "User e-mail address updated.");
                    } else {
                        Snackbar.make(parent, Objects.requireNonNull(task.getException()).getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                        Log.d("USER_EMAIL_UPDATED", task.getException().toString());
                    }
                }
            });
    }

    /**
     * Sends an e-mail verification when user updates the e-mail address.
     */
    private static void sendEmailVerification(final FirebaseUser currentUser, final Context context) {
        currentUser.sendEmailVerification()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getText(R.string.email_verification_alert_title).toString())
                                .setMessage(context.getText(R.string.email_verification_alert_message).toString())
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                //reloadCurrentUser();
                                            }
                                        }).create().show();

                        Log.d("USER_EMAIL_VERIF", "Verification e-mail sent.");
                    }
                }
            });
    }

    /**
     * Updates the current user password on FirebaseAuth.
     * @param currentUser the current user.
     * @param new_password the new password.
     */
    public static void updateUserPassword(final FirebaseUser currentUser, String new_password, final View parent) {
        currentUser.updatePassword(new_password)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Snackbar.make(parent, parent.getContext().getString(R.string.update_email_success),
                                Snackbar.LENGTH_SHORT).show();
                        Log.d("USER_PW_UPDATED", "User password address updated.");
                    } else {
                        Snackbar.make(parent, Objects.requireNonNull(task.getException()).getMessage(),
                                Snackbar.LENGTH_SHORT).show();
                        Log.d("USER_PW_UPDATED", task.getException().toString());
                    }
                }
            });
    }
}

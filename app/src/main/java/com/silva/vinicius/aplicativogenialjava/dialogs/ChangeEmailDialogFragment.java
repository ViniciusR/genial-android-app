package com.silva.vinicius.aplicativogenialjava.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.LayoutInflater;


import com.silva.vinicius.aplicativogenialjava.R;
import com.silva.vinicius.aplicativogenialjava.enums.TextValidatorTypes;
import com.silva.vinicius.aplicativogenialjava.utils.TextValidator;
import com.silva.vinicius.aplicativogenialjava.utils.ValidatorOption;

import java.util.Objects;

public class ChangeEmailDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ChangeEmailDialogListener {
        void onChangeEmailDialogPositiveClick(DialogFragment dialog);
        void onChangeEmailDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    ChangeEmailDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ChangeEmailDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ChangeEmailDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        assert args != null;
        String current_email = args.getString("CURRENT_EMAIL");

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.change_email_dialog, null);
        builder.setView(layout);

        TextInputEditText email_editText            =  layout.findViewById(R.id.email_editText);
        TextInputEditText current_password_editText =  layout.findViewById(R.id.current_password_editText);

        email_editText.setText(current_email);

        // Set text input validators
        TextValidator.setValidators(getActivity(), email_editText, new ValidatorOption(TextValidatorTypes.EMAIL));
        TextValidator.setValidators(getActivity(), current_password_editText, new ValidatorOption(TextValidatorTypes.NOT_EMPTY));

        builder.setTitle(R.string.change_email_alert_title)
                .setMessage(R.string.change_email_alert_message)
                .setPositiveButton(R.string.change_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onChangeEmailDialogPositiveClick(ChangeEmailDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onChangeEmailDialogNegativeClick(ChangeEmailDialogFragment.this);
                    }
                });
        return builder.create();
    }
}

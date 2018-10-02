package com.silva.vinicius.aplicativogenialjava.utils;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import com.silva.vinicius.aplicativogenialjava.R;
import com.silva.vinicius.aplicativogenialjava.enums.TextValidatorTypes;


public abstract class TextValidator implements TextWatcher {
    private final TextView textView;

    private TextValidator(TextView textView) {
        this.textView = textView;
    }

    public abstract void validate(TextView textView, String text);

    @Override
    final public void afterTextChanged(Editable s) {
        String text = textView.getText().toString();
        validate(textView, text);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

    /**
     * Checks if an e-mail address is valid.
     * @param target the e-mail address.
     * @return true if the e-mail address is valid.
     */
    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    /**
     * Validate the field inputs in real time, while the user is typing.
     */
    public static void setValidators(final Context context, EditText editableView, ValidatorOption... validators) {

        for(final ValidatorOption validator : validators){

            if (validator.getType().equals(TextValidatorTypes.EMAIL)) {

                editableView.addTextChangedListener(new TextValidator(editableView) {
                    @Override
                    public void validate(TextView textView, String text) {
                        TextInputLayout til = (TextInputLayout) textView.getParent().getParent();
                        if (!TextValidator.isValidEmail(text)) {
                            til.setError(context.getResources().getString(R.string.error_invalid_email));
                        } else {
                            til.setError(null);
                        }
                    }
                });
            }

            if (validator.getType().equals(TextValidatorTypes.NOT_EMPTY)) {

                editableView.addTextChangedListener(new TextValidator(editableView) {
                    @Override
                    public void validate(TextView textView, String text) {
                        TextInputLayout til = (TextInputLayout) textView.getParent().getParent();
                        if (text.isEmpty()) {
                            til.setError(context.getResources().getString(R.string.fui_required_field));
                        } else {
                            til.setError(null);
                        }
                    }
                });
            }

            if (validator.getType().equals(TextValidatorTypes.MIN_LENGTH)) {

                editableView.addTextChangedListener(new TextValidator(editableView) {
                    @Override
                    public void validate(TextView textView, String text) {
                        TextInputLayout til = (TextInputLayout) textView.getParent().getParent();
                        if (text.length() < validator.getValue()) {
                            til.setError("");
                        } else {
                            til.setError(null);
                        }
                    }
                });
            }

            if (validator.getType().equals(TextValidatorTypes.MAX_LENGTH)) {

                editableView.addTextChangedListener(new TextValidator(editableView) {
                    @Override
                    public void validate(TextView textView, String text) {
                        TextInputLayout til = (TextInputLayout) textView.getParent().getParent();
                        if (text.length() > validator.getValue()) {
                            til.setError("");
                        } else {
                            til.setError(null);
                        }
                    }
                });
            }
        }
    }

    /**
     * Checks if there is any input errors in a set of TextInputEditText inputs.
     * @param inputs the set of TextInputEditText inputs.
     * @return true if there is any error.
     */
    public static Boolean hasInputErrors(TextInputEditText ... inputs) {

        for(TextInputEditText input : inputs) {
            TextInputLayout til = (TextInputLayout) input.getParent().getParent();
            if (til.getError() != null) {
                return true;
            }
        }

        return false;
    }
}

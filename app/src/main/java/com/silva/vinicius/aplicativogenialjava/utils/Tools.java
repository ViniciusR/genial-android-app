package com.silva.vinicius.aplicativogenialjava.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import java.util.Objects;

@SuppressWarnings("unused")
public final class Tools {

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFromDialog(Context context, Dialog dialog) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(Objects.requireNonNull(Objects.requireNonNull(dialog.getWindow()).getCurrentFocus()).getWindowToken(), 0);
    }

    /**
     * Converts dip into its equivalent px
     * @param dp the dp unit.
     * @return the value in pixels.
     */
    public static float dpToPx(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().getDisplayMetrics()
        );
    }
}

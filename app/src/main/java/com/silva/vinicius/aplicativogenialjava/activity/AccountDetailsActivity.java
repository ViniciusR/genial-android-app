package com.silva.vinicius.aplicativogenialjava.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.silva.vinicius.aplicativogenialjava.R;
import com.silva.vinicius.aplicativogenialjava.config.FirebaseConfig;
import com.silva.vinicius.aplicativogenialjava.dialogs.ChangeEmailDialogFragment;
import com.silva.vinicius.aplicativogenialjava.dialogs.ChangePasswordDialogFragment;
import com.silva.vinicius.aplicativogenialjava.dialogs.DeleteAccountEmailDialogFragment;
import com.silva.vinicius.aplicativogenialjava.utils.AuthUtils;
import com.silva.vinicius.aplicativogenialjava.utils.Tools;
import com.silva.vinicius.aplicativogenialjava.utils.ValidatorOption;
import com.silva.vinicius.aplicativogenialjava.enums.TextValidatorTypes;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.silva.vinicius.aplicativogenialjava.models.User;
import com.silva.vinicius.aplicativogenialjava.utils.CircleTransform;
import com.silva.vinicius.aplicativogenialjava.utils.TextValidator;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class AccountDetailsActivity extends AppCompatActivity implements
        View.OnClickListener,
        ChangeEmailDialogFragment.ChangeEmailDialogListener,
        ChangePasswordDialogFragment.ChangePasswordDialogListener,
        DeleteAccountEmailDialogFragment.DeleteAccountEmailDialogListener,
        DatePickerDialog.OnDateSetListener{

    private DatabaseReference userDatabaseReference;
    private StorageReference userStorageReference;
    private FirebaseUser currentUser;
    private ImageView user_profile_picture;
    private TextView email_textView;
    private TextInputEditText name_editText;
    private TextInputEditText birthday_editText;
    private Spinner gender_spinner;
    private FrameLayout progressBarHolder;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsing_toolbar;
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        user_profile_picture = findViewById(R.id.user_profile_picture);
        email_textView       = findViewById(R.id.email_textView);
        name_editText        = findViewById(R.id.name_editText);
        birthday_editText    = findViewById(R.id.birthday_editText);
        gender_spinner       = findViewById(R.id.spinner_user_gender);
        progressBarHolder    = findViewById(R.id.progressBarHolder);
        collapsing_toolbar   = findViewById(R.id.collapsing_toolbar);
        toolbar              = findViewById(R.id.toolbar);
        simpleDateFormat     = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        userDatabaseReference = FirebaseConfig.getDatabaseReference().child("users");
        userStorageReference  = FirebaseConfig.getStorageReference().child("images/profile_pictures/");
        currentUser           = FirebaseConfig.getAuthReference().getCurrentUser();

        setInputsValidators();
        setButtonsOptions();
        setToolbarMenus();
        updateUI(currentUser);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    //================================================================================
    // Data handling related functions
    //================================================================================

    /**
     * Signs out the current user.
     */
    private void signOut() {
        progressBarHolder.setVisibility(View.VISIBLE);

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@Nullable Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    /**
     * Deletes the current user's account_details_inputs.
     */
    private void deleteAccount() {

        final String current_user_id = Objects.requireNonNull(currentUser).getUid();
        final User user_model = new User(current_user_id);

        progressBarHolder.setVisibility(View.VISIBLE);

        //Because of the Firebase Storage rules, the file can not be deleted after the account is deleted.
        //See the line "request.auth != null"
        if (currentUser.getPhotoUrl() != null) {
            //The file name is currentUser.getUid()
            deleteCurrentProfilePicture(currentUser.getUid());
        }

        currentUser
            .delete()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@Nullable Task<Void> task) {
                    assert task != null;
                    if (task.isSuccessful()) {
                        Log.i("deleteaccount", "deleteAccountAuth:success");
                        user_model.delete();
                        updateUI(null);
                    } else {
                        new AlertDialog.Builder(AccountDetailsActivity.this)
                                .setTitle(getText(R.string.delete_account_alert_reauth_title).toString())
                                .setMessage(getText(R.string.delete_account_alert_reauth_message).toString())
                                .setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                reloadCurrentUser();
                                            }
                                        }).create().show();
                        Log.w("deleteaccount", "deleteAccountAuth:failure", task.getException());
                    }

                    progressBarHolder.setVisibility(View.GONE);
                }
            });
    }

    /**
     * Updates the current FirebaseUser profile data and refresh "My account" page.
     */
    private void submit(UserProfileChangeRequest profileUpdates) {

        assert currentUser != null;

        progressBarHolder.setVisibility(View.VISIBLE);

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        updateUserModel();
                        //Update the page
                        reloadCurrentUser();

                        Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.profile_updated_success),
                                Snackbar.LENGTH_SHORT).show();
                        Log.d("USER_PROFILE_UPDATED", "User profile updated.");
                    } else {
                        String message;
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch(FirebaseNetworkException e) {
                            Log.d("USER_PROFILE_UPDATED", e.getMessage());
                            message = getString(R.string.no_internet_connection);
                        } catch(Exception e) {
                            Log.e("USER_PROFILE_UPDATED", e.getMessage());
                            message = getString(R.string.error_update_profile);
                        }

                        Snackbar.make(findViewById(R.id.account_details_activity), message,
                                Snackbar.LENGTH_SHORT).show();
                    }

                    progressBarHolder.setVisibility(View.GONE);
                }
            });
    }

    /**
     * Retrieve the current user from Firebase Realtime FirebaseConfig and updates its data.
     */
    private void updateUserModel() {
        User updated_user = new User(currentUser.getUid());
        updated_user.setName(currentUser.getDisplayName());
        updated_user.setBirthday(Objects.requireNonNull(birthday_editText.getText()).toString());
        updated_user.setPhoto_url(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
        updated_user.setGender(identifyUserGender());
        updated_user.setUpdated(true);

        updated_user.update();
    }

    /**
     * Identify the user gender selected on the spinner in the layout.
     * 0 is female, 1 is male.
     * @return "F" or "M".
     */
    private String identifyUserGender() {

        Integer selected_position = gender_spinner.getSelectedItemPosition();

        //0 is Female. 1 is Male.
        if (selected_position == 0) {
            return "F";
        }
        return "M";
    }

    /**
     * Deletes the file on Firebase Storage of the current profile picture of the current user.
     * @param file_name the file name of the user's profile picture.
     */
    private void deleteCurrentProfilePicture(String file_name) {

        if (!file_name.isEmpty()) {
            userStorageReference.child(file_name).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("FIREBASE_STORAGE", "success: deleted current profile picture");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("FIREBASE_STORAGE", exception.getMessage());
                }
            });
        }
    }

    //================================================================================
    // UI related functions
    //================================================================================

    /**
     * Retrieves the current user from Firebase Realtime FirebaseConfig and load its data on the layout.
     */
    private void loadUser(final FirebaseUser user) {

        userDatabaseReference.child("/"+user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user_model = dataSnapshot.getValue(User.class);

                if (user_model != null) {

                    name_editText.setText(user_model.getName());
                    email_textView.setText(user.getEmail());
                    collapsing_toolbar.setTitle(user_model.getName());

                    if (user_model.getBirthday() != null) {

                        birthday_editText.setText(user_model.getBirthday());
                    }

                    if (user_model.getGender() != null) {
                        switch (user_model.getGender()) {
                            case "F":
                                gender_spinner.setSelection(0);
                                break;
                            case "M":
                                gender_spinner.setSelection(1);
                                break;
                            default:
                                //
                                break;
                        }
                    }

                    Picasso.get()
                        .load(user_model.getPhoto_url())
                        .transform(new CircleTransform())
                        .resize(Math.round(Tools.dpToPx(100)), Math.round(Tools.dpToPx(100)))
                        .centerCrop()
                        .placeholder(R.drawable.ic_add_a_photo)
                        .into(user_profile_picture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Sets the validators and masks for inputs.
     */
    public void setInputsValidators() {
        //Input validator
        TextValidator.setValidators(AccountDetailsActivity.this, name_editText, new ValidatorOption(TextValidatorTypes.NOT_EMPTY));
    }

    /**
     * Sets the buttons of the screen and its visibilities.
     */
    public void setButtonsOptions() {
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.delete_account_button).setOnClickListener(this);
        user_profile_picture.setOnClickListener(this);

        if (Objects.requireNonNull(FirebaseConfig.getSignInProvider(currentUser)).equals("password")) {
            findViewById(R.id.change_email_button).setVisibility(View.VISIBLE);
            findViewById(R.id.change_password_button).setVisibility(View.VISIBLE);
            findViewById(R.id.change_email_button).setOnClickListener(this);
            findViewById(R.id.change_password_button).setOnClickListener(this);
        } else {
            findViewById(R.id.change_email_button).setVisibility(View.GONE);
            findViewById(R.id.change_password_button).setVisibility(View.GONE);
        }
    }

    /**
     * Updates the UI.
     */
    private void updateUI(FirebaseUser user) {
        if (user == null) {
            Intent intent = new Intent(AccountDetailsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            loadUser(user);
        }
    }

    /**
     * Sets the menus of the toolbar.
     */
    private void setToolbarMenus() {
        toolbar.inflateMenu(R.menu.account_details_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.button_save_account_details:
                        //Checks if all required inputs are valid.
                        if (!hasInputErrors(name_editText)) {

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(Objects.requireNonNull(name_editText.getText()).toString())
                                    .build();

                            submit(profileUpdates);
                        } else {
                            Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.error_check_inputs),
                                    Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
                //Hides the keyboard
                Tools.hideKeyboard(AccountDetailsActivity.this);
                return true;
            }
        });
    }

    /**
     * Shows a date picker for birthday.
     * @param view the edittext for birthday.
     */
    public void showDatePicker(View view) {

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Date current_birthday;
        try {
            int default_year = 1900;
            int default_month = 0;
            int default_day = 0;

            if (!Objects.requireNonNull(birthday_editText.getText()).toString().isEmpty()) {
                current_birthday = df.parse(birthday_editText.getText().toString());
                default_year += current_birthday.getYear();
                default_month += current_birthday.getMonth();
                default_day = current_birthday.getDate();
            }

            new SpinnerDatePickerDialogBuilder()
                .context(AccountDetailsActivity.this)
                .callback(AccountDetailsActivity.this)
                .showTitle(true)
                .showDaySpinner(true)
                .defaultDate(default_year, default_month, default_day)
                .maxDate(2018, 0, 1)
                .build()
                .show();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads the current user data and refreshes the page.
     */
    private void reloadCurrentUser() {
        currentUser
                .reload()
                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void aVoid) {
                        loadUser(currentUser);
                    }
                });
    }

    /**
     * Shows and UI to user delete account_details_inputs depending on the sign in provider.
     */
    private void showDeleteAccountUI() {
        if (Objects.requireNonNull(FirebaseConfig.getSignInProvider(currentUser)).equals("password")) {
            showDeleteAccountEmailDialog();
        } else {
            showDeleteAccountFederatedDialog();
        }
    }

    /**
     * Checks if there is any input with error.
     * @return true if there is any input with error.
     */
    private boolean hasInputErrors(TextInputEditText... editTexts) {
        return TextValidator.hasInputErrors(editTexts);
    }

    //================================================================================
    // Dialogs related functions
    //================================================================================

    /**
     * Shows an alert dialog to confirm signing out.
     */
    private void showSignOutDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        signOut();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(AccountDetailsActivity.this);

        builder.setTitle(getString(R.string.sign_out_alert_title));
        builder.setMessage(getString(R.string.sign_out_alert_message))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    /**
     * Builds and shows the alert dialog with editTexts to user delete the account_details_inputs. For Federated providers only.
     */
    private void showDeleteAccountFederatedDialog() {
        new AlertDialog.Builder(AccountDetailsActivity.this)
                .setTitle(getText(R.string.delete_account_alert_title).toString())
                .setMessage(getText(R.string.delete_account_alert_message).toString())
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(getString(R.string.button_delete_account),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAccount();
                            }
                        }).create().show();
    }

    /**
     * Creates and shows a dialog for user to change the e-mail address.
     */
    private void showChangeEmailDialog() {
        DialogFragment dialog = new ChangeEmailDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString("CURRENT_EMAIL",currentUser.getEmail());
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), "changeEmail");
    }

    /**
     * Creates and shows a dialog for user to change the password.
     */
    private void showChangePasswordDialog() {
        DialogFragment dialog = new ChangePasswordDialogFragment();
        dialog.show(getSupportFragmentManager(), "changePassword");
    }

    /**
     * Creates and shows a dialog for user to delete account when the sign in type is through e-mail address.
     */
    private void showDeleteAccountEmailDialog() {
        DialogFragment dialog = new DeleteAccountEmailDialogFragment();
        dialog.show(getSupportFragmentManager(), "deleteAccountEmail");
    }

    /**
     * Shows a picker for user to choose the profile picture.
     */
    private void showProfilePicturePicker() {
        EasyImage.configuration(this)
                .setAllowMultiplePickInGallery(false);

        EasyImage.openChooserWithGallery(this, getString(R.string.select_profile_picture_location), 0);
    }

    //================================================================================
    // Events and click listeners functions
    //================================================================================

    /**
     * Reauthenticates and updates the user e-mail address when the dialog's positive button is clicked.
     * @param dialog the alert dialog.
     */
    @Override
    public void onChangeEmailDialogPositiveClick(final DialogFragment dialog) {

        progressBarHolder.setVisibility(View.VISIBLE);

        final TextInputEditText email_editText            = dialog.getDialog().findViewById(R.id.email_editText);
        final TextInputEditText current_password_editText = dialog.getDialog().findViewById(R.id.current_password_editText);

        final String new_email = Objects.requireNonNull(email_editText.getText()).toString();
        String current_password = Objects.requireNonNull(current_password_editText.getText()).toString();

        if (hasInputErrors(email_editText, current_password_editText)) {
            progressBarHolder.setVisibility(View.GONE);
            Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.error_check_inputs), Snackbar.LENGTH_SHORT).show();
        } else {
            if (!current_password.isEmpty()) {
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), current_password);

                currentUser.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Check if user has changed the e-mail address
                                if (!new_email.equals(currentUser.getEmail())) {
                                    dialog.dismiss();
                                    AuthUtils.updateUserEmailAddress(currentUser, new_email, findViewById(R.id.account_details_activity));
                                    reloadCurrentUser();
                                }
                            } else {
                                Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.fui_error_invalid_password), Snackbar.LENGTH_SHORT).show();
                                Log.w("CHANGE_EMAIL", "changeEmail:failure", task.getException());
                            }

                            progressBarHolder.setVisibility(View.GONE);
                        }
                    });
            } else progressBarHolder.setVisibility(View.GONE);
        }
        Tools.hideKeyboardFromDialog(this, dialog.getDialog());
    }

    @Override
    public void onChangeEmailDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    /**
     * Reauthenticates and updates the user password when the dialog's positive button is clicked.
     * @param dialog the alert dialog.
     */
    @Override
    public void onChangePasswordDialogPositiveClick(final DialogFragment dialog) {

        progressBarHolder.setVisibility(View.VISIBLE);

        final TextInputEditText new_password_editText     = dialog.getDialog().findViewById(R.id.new_password_editText);
        final TextInputEditText current_password_editText = dialog.getDialog().findViewById(R.id.current_password_editText);

        final String new_password = Objects.requireNonNull(new_password_editText.getText()).toString();
        String current_password = Objects.requireNonNull(current_password_editText.getText()).toString();

        if (!current_password.isEmpty()) {
            AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), current_password);

            currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            AuthUtils.updateUserPassword(currentUser, new_password, findViewById(R.id.account_details_activity));
                        } else {
                            Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.fui_error_invalid_password), Snackbar.LENGTH_SHORT).show();
                            Log.w("REAUTH_CHANGE_PW", "reauth_changeEmail:failure", task.getException());
                        }

                        progressBarHolder.setVisibility(View.GONE);
                    }
                });
        } else progressBarHolder.setVisibility(View.GONE);
        Tools.hideKeyboardFromDialog(this, dialog.getDialog());
    }

    @Override
    public void onChangePasswordDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    /**
     * Reauthenticates and deletes the user account when the dialog's positive button is clicked.
     * @param dialog the alert dialog.
     */
    @Override
    public void onDeleteAccountEmailDialogPositiveClick(final DialogFragment dialog) {
        progressBarHolder.setVisibility(View.VISIBLE);

        final TextInputEditText current_password_editText = dialog.getDialog().findViewById(R.id.current_password_editText);

        String current_password = Objects.requireNonNull(current_password_editText.getText()).toString();

        if (hasInputErrors(current_password_editText)) {
            progressBarHolder.setVisibility(View.GONE);
            Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.error_check_inputs), Snackbar.LENGTH_SHORT).show();
        } else {
            if (!current_password.isEmpty()) {
                AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), current_password);

                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    deleteAccount();
                                } else {
                                    Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.fui_error_invalid_password), Snackbar.LENGTH_SHORT).show();
                                    Log.w("DELETE_ACC", "reauthenticate:failure", task.getException());
                                }
                                progressBarHolder.setVisibility(View.GONE);
                            }
                        });
            } else progressBarHolder.setVisibility(View.GONE);
        }
        Tools.hideKeyboardFromDialog(this, dialog.getDialog());
    }

    @Override
    public void onDeleteAccountEmailDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.sign_out_button) {
            showSignOutDialog();
        }
        if (i == R.id.delete_account_button) {
            showDeleteAccountUI();
        }
        if (i == R.id.change_email_button) {
            showChangeEmailDialog();
        }
        if (i == R.id.change_password_button) {
            showChangePasswordDialog();
        }
        if (i == R.id.user_profile_picture) {
            showProfilePicturePicker();
        }
    }

    @Override
    public void onBackPressed() {
        showSignOutDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Log.d("PROFILE_PICTURE", "error:" + e.getMessage());
            }

            @Override
            public void onImagesPicked(@NonNull List<File> list, EasyImage.ImageSource imageSource, int i) {

                 if (currentUser.getPhotoUrl() != null) {
                     deleteCurrentProfilePicture(currentUser.getUid());
                 }

                Uri file = Uri.fromFile(list.get(0));

                progressBarHolder.setVisibility(View.VISIBLE);

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                byte[] data = baos.toByteArray();

                //The file name will the currentUser.getUid()
                UploadTask uploadTask = userStorageReference.child(currentUser.getUid()).putBytes(data);

                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        return Objects.requireNonNull(Objects.requireNonNull(task.getResult().getMetadata()).getReference()).getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();

                            UserProfileChangeRequest profilePicture = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri)
                                    .build();

                            submit(profilePicture);

                            Log.d("PROFILE_PICTURE", "success:Profile picture updated.");
                        } else {
                            Snackbar.make(findViewById(R.id.account_details_activity), getString(R.string.error_update_profile),
                                    Snackbar.LENGTH_SHORT).show();

                            Log.d("PROFILE_PICTURE", "error:"+Objects.requireNonNull(task.getException()).getMessage());
                        }
                        progressBarHolder.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        birthday_editText.setText(simpleDateFormat.format(calendar.getTime()));
    }
}

package com.example.cne_commute;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.common.SignInButton;

import android.text.TextWatcher;
import android.text.Editable;




public class CommuterSignUpActivity extends AppCompatActivity {

    private static final String TAG = "CommuterSignUpActivity";
    private static final String CHANNEL_ID = "signup_notifications";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPasswordVisible = false;

    private GoogleSignInClient mGoogleSignInClient;

    private EditText firstNameEditText, surnameEditText, emailEditText, contactEditText, passwordEditText;

    private ImageView passwordEyeIcon;
    private CheckBox termsCheckbox;
    private Button signUpButton;
    private SignInButton googleSignInButton;
    private TextView loginLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commuter_sign_up);

        Log.d(TAG, "onCreate: CommuterSignUpActivity launched");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "FirebaseAuth and Firestore initialized");

        // Assign UI components to class-level variables
        firstNameEditText = findViewById(R.id.first_name);
        surnameEditText = findViewById(R.id.surname);
        emailEditText = findViewById(R.id.email);
        contactEditText = findViewById(R.id.contact_number);
        passwordEditText = findViewById(R.id.password);
        passwordEyeIcon = findViewById(R.id.password_eye_icon);
        termsCheckbox = findViewById(R.id.terms_checkbox);
        signUpButton = findViewById(R.id.sign_up_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        loginLink = findViewById(R.id.login_link);
        Log.d(TAG, "UI components assigned");
        contactEditText.setText("+63");
        contactEditText.setSelection(contactEditText.getText().length());

        contactEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                if (!s.toString().startsWith("+63")) {
                    contactEditText.setText("+63");
                    contactEditText.setSelection(contactEditText.getText().length());
                }

                isFormatting = false;
            }
        });


        createNotificationChannel();
        Log.d(TAG, "Notification channel created");

        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    REQUEST_NOTIFICATION_PERMISSION);
            Log.d(TAG, "Notification permission requested");
        }

        termsCheckbox.setOnClickListener(v -> {
            Log.d(TAG, "Terms checkbox clicked");
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_terms_policy, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            Button okButton = dialogView.findViewById(R.id.terms_ok_button);
            okButton.setOnClickListener(btn -> dialog.dismiss());

            dialog.show();
        });

        signUpButton.setOnClickListener(v -> {
            Log.d(TAG, "Sign-up button clicked");
            signUpUser(
                    firstNameEditText.getText().toString().trim(),
                    surnameEditText.getText().toString().trim(),
                    emailEditText.getText().toString().trim(),
                    contactEditText.getText().toString().trim(),
                    passwordEditText.getText().toString().trim(),
                    termsCheckbox,
                    firstNameEditText, surnameEditText, emailEditText, contactEditText, passwordEditText
            );
        });

        loginLink.setOnClickListener(v -> {
            Log.d(TAG, "Login link clicked");
            Intent intent = new Intent(CommuterSignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });

        passwordEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            Log.d(TAG, "Password visibility toggled: " + isPasswordVisible);
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_on);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_off);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Google Sign-In setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "GoogleSignInClient initialized");

        // Google Sign-In button click listener with forced sign-out
        googleSignInButton.setOnClickListener(v -> {
            Log.d(TAG, "Google Sign-In button clicked");
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Log.d(TAG, "Google Sign-In client signed out");
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 100); // RC_SIGN_IN = 100
            });
        });
        for (int i = 0; i < googleSignInButton.getChildCount(); i++) {
            View child = googleSignInButton.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setText("Continue with Google");
                break;
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Log.d(TAG, "Google Sign-In intent returned, processing result…");

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-In successful. Account: " + account.getEmail());

                // Pass both ID token and account to autofill the form
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                Log.e(TAG, "Google Sign-In failed: " + e.getStatusCode() + " - " + e.getMessage(), e);
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Use account info to autofill
                            String fullName = account.getDisplayName(); // e.g., "Krizza Heart Esperas"
                            String email = account.getEmail();

                            // Split full name into first name and surname
                            String[] nameParts = fullName != null ? fullName.trim().split(" ") : new String[]{};
                            String surname = nameParts.length > 0 ? nameParts[nameParts.length - 1] : "";
                            String firstName = nameParts.length > 1 ? fullName.replace(" " + surname, "") : "";

                            // Autofill the form
                            firstNameEditText.setText(firstName);
                            surnameEditText.setText(surname);
                            emailEditText.setText(email);
                            emailEditText.setEnabled(false); // Optional: lock email field

                            Toast.makeText(this, "Google account detected. Please complete your profile.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception e = task.getException();
                        Log.e(TAG, "Firebase Auth with Google failed", e); // Logs full stack trace
                        Toast.makeText(this, "Authentication Failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void signUpUser(@NonNull String firstName, @NonNull String surname, @NonNull String email,
                            @NonNull String contact, @NonNull String password,
                            @NonNull CheckBox termsCheckbox,
                            @NonNull EditText firstNameEditText, @NonNull EditText surnameEditText,
                            @NonNull EditText emailEditText, @NonNull EditText contactEditText,
                            @NonNull EditText passwordEditText) {

        if (firstName.isEmpty()) {
            firstNameEditText.setError("Enter your first name");
            firstNameEditText.requestFocus();
            return;
        }

        if (surname.isEmpty()) {
            surnameEditText.setError("Enter your surname");
            surnameEditText.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (!contact.startsWith("+63") || contact.length() != 13 || !contact.matches("\\+63\\d{10}")) {
            contactEditText.setError("Enter a valid Philippine mobile number (e.g., +639171234567)");
            contactEditText.requestFocus();
            return;
        }


        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        //  Check if user came from Google Sign-In
        boolean isGoogleUser = !emailEditText.isEnabled();

        if (isGoogleUser) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                if (password.isEmpty() || password.length() < 6) {
                    passwordEditText.setError("Enter a valid password (at least 6 characters)");
                    passwordEditText.requestFocus();
                    return;
                }

                // Create email/password account using Google email and commuter's chosen password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser newUser = mAuth.getCurrentUser();
                                if (newUser != null) {
                                    String fullName = firstName + " " + surname;
                                    saveUserDataToFirestore(newUser.getUid(), fullName, email, contact);
                                    saveToSharedPreferences(fullName, contact);

                                    Intent intent = new Intent(CommuterSignUpActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finishAffinity();

                                    sendNotification();
                                }
                            } else {
                                Log.e(TAG, "Password setup failed: " + task.getException().getMessage());
                                Toast.makeText(this, "Failed to set password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }


        } else {
            //  Manual sign-up flow
            if (password.isEmpty() || password.length() < 6) {
                passwordEditText.setError("Enter a valid password (at least 6 characters)");
                passwordEditText.requestFocus();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String fullName = firstName + " " + surname;
                                saveUserDataToFirestore(user.getUid(), fullName, email, contact);
                                saveToSharedPreferences(fullName, contact);

                                Intent intent = new Intent(CommuterSignUpActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finishAffinity();

                                sendNotification();
                            }
                        } else {
                            Log.e(TAG, "Authentication Failed: " + task.getException().getMessage());
                            Toast.makeText(CommuterSignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void saveUserDataToFirestore(@NonNull String uid, @NonNull String fullName,
                                         @NonNull String email, @NonNull String contact) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("commuterId", uid); //  Firebase UID
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("contact", contact);
        userData.put("userType", "Commuter");

        //  Detect sign-up method based on email field state
        String signUpMethod = emailEditText.isEnabled() ? "manual" : "google";
        userData.put("signUpMethod", signUpMethod);

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data: " + e.getMessage());
                    Toast.makeText(CommuterSignUpActivity.this, "Failed to save user data. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveToSharedPreferences(String fullName, String contact) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
        editor.putString("commuter_name", fullName);
        editor.putString("commuter_contact", contact);

        //  Add email and sign-up method
        String email = emailEditText.getText().toString().trim();
        String signUpMethod = emailEditText.isEnabled() ? "manual" : "google";

        editor.putString("email_address", email);
        editor.putString("sign_up_method", signUpMethod);

        editor.apply();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SignUp Notification";
            String description = "Notifications for sign-up process";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED) {

            // Optional: personalize with commuter name from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String name = prefs.getString("commuter_name", "Commuter");

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Welcome, " + name + "!")
                    .setContentText("Your commuter account has been successfully created. Enjoy your journey!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, builder.build());
        }
    }

}

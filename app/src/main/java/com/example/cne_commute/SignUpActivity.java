package com.example.cne_commute;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private static final String CHANNEL_ID = "signup_notifications";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText usernameEditText = findViewById(R.id.username);
        EditText emailEditText = findViewById(R.id.email);
        EditText passwordEditText = findViewById(R.id.password);
        EditText confirmPasswordEditText = findViewById(R.id.confirm_password);
        ImageView passwordEyeIcon = findViewById(R.id.password_eye_icon);
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView loginLink = findViewById(R.id.login_link);

        createNotificationChannel();

        // Check for notification permission
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    REQUEST_NOTIFICATION_PERMISSION);
        }

        // Handle Sign Up button click
        signUpButton.setOnClickListener(v -> signUpUser(
                usernameEditText.getText().toString().trim(),
                emailEditText.getText().toString().trim(),
                passwordEditText.getText().toString().trim(),
                confirmPasswordEditText.getText().toString().trim(),
                usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText
        ));

        // Handle Login link click to navigate to SignInActivity
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish(); // Close SignUpActivity after navigating
        });

        // Toggle password visibility
        passwordEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_on); // Replace with the visible icon
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_off); // Replace with the hidden icon
            }
            passwordEditText.setSelection(passwordEditText.getText().length()); // Move cursor to the end
        });
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

    private void signUpUser(@NonNull String username, @NonNull String email, @NonNull String password,
                            @NonNull String confirmPassword,
                            @NonNull EditText usernameEditText, @NonNull EditText emailEditText,
                            @NonNull EditText passwordEditText, @NonNull EditText confirmPasswordEditText) {

        // Validate inputs
        if (username.isEmpty()) {
            usernameEditText.setError("Enter a valid username");
            usernameEditText.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Enter a valid password (at least 6 characters)");
            passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Directly navigate to the next screen
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            intent.putExtra("username", username); // Pass data if needed
                            startActivity(intent);
                            finishAffinity(); // Close SignUpActivity

                            // Save user details in Firestore in the background
                            saveUserDataToFirestore(user.getUid(), username, email);

                            // Send notification
                            sendNotification();
                        }
                    } else {
                        Log.e(TAG, "Authentication Failed: " + task.getException().getMessage());
                        Toast.makeText(SignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserDataToFirestore(@NonNull String uid, @NonNull String username, @NonNull String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("userType", "Commuter");

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User data saved successfully"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data: " + e.getMessage());
                    // NEW: Show user-friendly error message
                    Toast.makeText(SignUpActivity.this, "Failed to save user data. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotification() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
                    .setContentTitle("Account Created")
                    .setContentText("Your account has been successfully created.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, builder.build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

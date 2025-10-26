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

public class OperatorSignUpActivity extends AppCompatActivity {

    private static final String TAG = "OperatorSignUpActivity";
    private static final String CHANNEL_ID = "signup_notifications_operator";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText operatorNameEditText = findViewById(R.id.username);
        EditText operatorEmailEditText = findViewById(R.id.email);
        EditText operatorPasswordEditText = findViewById(R.id.password);
        EditText confirmPasswordEditText = findViewById(R.id.confirm_password);
        ImageView passwordEyeIcon = findViewById(R.id.password_eye_icon);
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView loginLink = findViewById(R.id.login_link);

        createNotificationChannel();

        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.POST_NOTIFICATIONS"},
                    REQUEST_NOTIFICATION_PERMISSION);
        }

        signUpButton.setOnClickListener(v -> signUpOperator(
                operatorNameEditText.getText().toString().trim(),
                operatorEmailEditText.getText().toString().trim(),
                operatorPasswordEditText.getText().toString().trim(),
                confirmPasswordEditText.getText().toString().trim(),
                operatorNameEditText, operatorEmailEditText, operatorPasswordEditText, confirmPasswordEditText
        ));

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(OperatorSignUpActivity.this, OperatorSignInActivity.class);
            startActivity(intent);
            finish();
        });


        passwordEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                operatorPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_on);
            } else {
                operatorPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_off);
            }
            operatorPasswordEditText.setSelection(operatorPasswordEditText.getText().length());
        });
    }

    private void signUpOperator(@NonNull String name, @NonNull String email, @NonNull String password,
                                @NonNull String confirmPassword,
                                @NonNull EditText nameInput, @NonNull EditText emailInput,
                                @NonNull EditText passInput, @NonNull EditText confirmPassInput) {

        if (name.isEmpty()) {
            nameInput.setError("Enter a valid name");
            nameInput.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passInput.setError("Password must be at least 6 characters");
            passInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPassInput.setError("Passwords do not match");
            confirmPassInput.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveOperatorData(user.getUid(), name, email);
                            sendNotification();

                            Intent intent = new Intent(OperatorSignUpActivity.this, OperatorHomeActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                    } else {
                        Log.e(TAG, "Registration failed: " + task.getException().getMessage());
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveOperatorData(@NonNull String uid, @NonNull String name, @NonNull String email) {
        Map<String, Object> operator = new HashMap<>();
        operator.put("username", name);
        operator.put("email", email);
        operator.put("userType", "operator"); // lowercase for consistency

        db.collection("users").document(uid)
                .set(operator)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Operator data saved"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving operator data: " + e.getMessage());
                    Toast.makeText(this, "Failed to save data. Try again later.", Toast.LENGTH_SHORT).show();
                });
    }


    private void sendNotification() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Account Created")
                    .setContentText("Your operator account was successfully created.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Operator Sign Up";
            String description = "Operator registration notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

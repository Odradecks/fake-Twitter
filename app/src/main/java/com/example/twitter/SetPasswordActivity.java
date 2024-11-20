package com.example.twitter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

public class SetPasswordActivity extends AppCompatActivity {
    private EditText setPasswordField, verifyPasswordField;
    private MaterialButton nextButton;
    private TextView emailTextView;
    private FirebaseAuth auth;
    private String email;
    private boolean isPassword1Visible = false, isPassword2Visible = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setpwd);

        auth = FirebaseAuth.getInstance();
        setPasswordField = findViewById(R.id.setPasswordField);
        verifyPasswordField = findViewById(R.id.verifyPasswordField);
        nextButton = findViewById(R.id.nextButton);
        emailTextView = findViewById(R.id.emailTextView);

        TextView cancelTextView = findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        if (email != null) {
            emailTextView.setText(email);
        }

        nextButton.setOnClickListener(v -> onNextClicked());

        // 设置 setPasswordField 的触摸事件监听器
        setPasswordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int rightEdge = setPasswordField.getRight() - setPasswordField.getCompoundPaddingRight();
                if (event.getX() > rightEdge) {
                    togglePasswordVisibility(setPasswordField, 1);
                }
            }
            return false;
        });

        // 设置 verifyPasswordField 的触摸事件监听器
        verifyPasswordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int rightEdge = verifyPasswordField.getRight() - verifyPasswordField.getCompoundPaddingRight();
                if (event.getX() > rightEdge) {
                    togglePasswordVisibility(verifyPasswordField, 2);
                }
            }
            return false;
        });
    }

    private void onNextClicked() {
        String password = setPasswordField.getText().toString().trim();
        String verifyPassword = verifyPasswordField.getText().toString().trim();

        if (password.isEmpty() || verifyPassword.isEmpty()) {
            Toast.makeText(this, "Please enter both password fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(verifyPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            createUserDocument(user.getUid(), email);
                            Toast.makeText(SetPasswordActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(SetPasswordActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }
                    } else {
                        Toast.makeText(SetPasswordActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserDocument(String uid, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String username = intent.getStringExtra("name");
        String birthday = intent.getStringExtra("birthday");
        // Create a User object with default data
        String defaultAvatarUrl = "https://150.158.15.33/images/default_avatar.png"; // Default avatar URL

        AppUser user = new AppUser(username, email, birthday, defaultAvatarUrl, System.currentTimeMillis());

        // Create the user document in Firestore
        db.collection("User").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SetPasswordActivity.this, "Error creating document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void togglePasswordVisibility(EditText passwordField, int fieldNumber) {
        int cursorPosition = passwordField.getSelectionStart();
        boolean isPasswordVisible = (fieldNumber == 1) ? isPassword1Visible : isPassword2Visible;

        if (isPasswordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.hide_password, 0);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.show_password, 0);
        }

        if (fieldNumber == 1) {
            isPassword1Visible = !isPassword1Visible;
        } else {
            isPassword2Visible = !isPassword2Visible;
        }

        passwordField.setSelection(cursorPosition);
    }
}

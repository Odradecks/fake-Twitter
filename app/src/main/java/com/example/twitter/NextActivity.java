package com.example.twitter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NextActivity extends AppCompatActivity {
    private EditText passwordField;
    private boolean isPasswordVisible = false;  // 用于记录密码是否可见
    private Button nextButton;
    private FirebaseAuth mAuth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        String email = getIntent().getStringExtra("email");  // 在这里获取到 email
        TextView accountTextView = findViewById(R.id.accountTextView);
        accountTextView.setText(email);

        TextView cancelTextView = findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(v -> finish());

        // 初始化成员变量
        passwordField = findViewById(R.id.inputPasswordField);
        nextButton = findViewById(R.id.nextButton);
        mAuth = FirebaseAuth.getInstance();

        // 设置登录按钮的点击事件
        nextButton.setOnClickListener(view -> login(email));

        // 设置密码可见性的切换
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int rightEdge = passwordField.getRight() - passwordField.getCompoundPaddingRight();
                if (event.getX() > rightEdge) {
                    togglePasswordVisibility(passwordField);
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText passwordField) {
        int cursorPosition = passwordField.getSelectionStart();

        if (isPasswordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.hide_password, 0);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.show_password, 0);
        }

        isPasswordVisible = !isPasswordVisible;
        passwordField.setSelection(cursorPosition);
    }

    private void login(String email) {
        String password = passwordField.getText().toString();

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password to continue logging in.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(NextActivity.this, "Log in successfully.", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPreferences = getSharedPreferences("status", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", true);  // 设置已登录状态
                        editor.putString("userId", user.getUid());  // 保存用户的 UID
                        editor.putString("userEmail", user.getEmail());
                        editor.apply();
                        goToHomepage();
                    } else {
                        Toast.makeText(NextActivity.this, "Wrong email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHomepage() {
        Intent intent = new Intent(NextActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}

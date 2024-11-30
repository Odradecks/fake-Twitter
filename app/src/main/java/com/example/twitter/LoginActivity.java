package com.example.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthException;

public class LoginActivity extends AppCompatActivity {

    private EditText accountField;
    private Button nextButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountField = findViewById(R.id.inputField);
        nextButton = findViewById(R.id.nextButton);
        mAuth = FirebaseAuth.getInstance();

        TextView cancelTextView = findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(v -> finish());
        TextView forgotPasswordTextView = findViewById(R.id.forgotPassword);
        forgotPasswordTextView.setOnClickListener(v -> {
            // 当点击“忘记密码”时，跳转到验证邮箱页面
            String email = accountField.getText().toString();
            if (!email.isEmpty()) {
                Intent intent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
                intent.putExtra("email", email);  // 传递邮箱信息
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your email first.", Toast.LENGTH_SHORT).show();
            }
        });

        nextButton.setOnClickListener(view -> checkAccountExistence());
    }

    private void checkAccountExistence() {
        String email = accountField.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email to continue logging in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 先验证邮箱是否已在Firebase Authentication中注册，使用Firebase Authentication进行登录（不需要密码）
        // 把邮箱传递到NextActivity
        mAuth.fetchSignInMethodsForEmail(email)  // 通过该方法检查邮箱是否已经被注册
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods().isEmpty()) {
                            // 如果邮箱未注册
                            Toast.makeText(LoginActivity.this, "This email address is not registered, please sign up first.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 邮箱已注册，跳转到密码验证页面
                            Intent intent = new Intent(LoginActivity.this, NextActivity.class);
                            intent.putExtra("email", email);  // 将邮箱传递给 NextActivity 进行密码验证
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to check if the email is registered: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordInput, verifyNewPasswordInput;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);

        newPasswordInput = findViewById(R.id.newPasswordInput);
        verifyNewPasswordInput = findViewById(R.id.verifyNewPasswordInput);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(view -> {
            String newPassword = newPasswordInput.getText().toString();
            String verifyPassword = verifyNewPasswordInput.getText().toString();

            if (newPassword.equals(verifyPassword)) {
                resetPassword(newPassword);
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String newPassword) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = getIntent().getStringExtra("email");  // 获取从上一个页面传递的邮箱

        // 使用 Firebase API 来重置密码
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 密码重置成功，返回登录界面
                        Toast.makeText(ResetPasswordActivity.this, "Password reset successful. You can now log in with your new password.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to reset password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.twitter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 使用 getApplicationContext() 获取上下文来初始化 Firebase
        FirebaseApp.initializeApp(getApplicationContext());

        // 获取 SharedPreferences 中保存的登录状态
        SharedPreferences sharedPreferences = getSharedPreferences("status", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        Intent intent;
        if (isLoggedIn) {
            // 如果已登录，跳转到主页
            intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Button loginButton = findViewById(R.id.logInButton);
            Button registerButton = findViewById(R.id.createAccountButton);

            // 为登录按钮添加点击事件
            loginButton.setOnClickListener(v -> {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            });

            // 为注册按钮添加点击事件
            registerButton.setOnClickListener(v -> {
                Intent registerIntent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(registerIntent);
            });
        }
    }
}

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

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class VerifyEmailActivity extends AppCompatActivity {

    private EditText emailInputField, verificationCodeInput;
    private Button nextButton;
    private String verificationCodeSentToEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifyemail);
        Log.d("onCreate", "display!");
        emailInputField = findViewById(R.id.emailInputField);
        verificationCodeInput = findViewById(R.id.verificationCodeInput);
        nextButton = findViewById(R.id.nextButton);

        // 获取从登录页面传递的邮箱
        String email = getIntent().getStringExtra("email");
        emailInputField.setText(email); // 只读邮箱

        // 生成并发送验证码
        sendVerificationCodeToEmail(email);

        // Next按钮点击事件，验证验证码是否一致
        nextButton.setOnClickListener(view -> {
            String enteredCode = verificationCodeInput.getText().toString();
            if (enteredCode.equals(verificationCodeSentToEmail)) {
                // 验证码正确，跳转到密码重置页面
                Intent intent = new Intent(VerifyEmailActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(VerifyEmailActivity.this, "Incorrect verification code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCodeToEmail(String email) {
        Log.d("sendVerificationCodeToEmail", "success");
        // 生成一个随机的验证码
        Random random = new Random();
        verificationCodeSentToEmail = String.format("%06d", random.nextInt(1000000));

        // 发送邮件
        sendEmail(email, verificationCodeSentToEmail);
    }

    private void sendEmail(String email, String verificationCode) {
        Log.d("sendEmail", "success");

        // 邮件发送的配置
        String fromEmail = "shu1993669100@gmail.com";  // 发件人邮箱（你需要使用真实的邮箱地址）
        String fromPassword = "";  // 发件人邮箱的密码
        String smtpHost = "smtp.gmail.com";  // 使用 Gmail SMTP 服务器

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", "587");

        // 使用新线程进行邮件发送
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建Session对象
                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    @Override
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(fromEmail, fromPassword);
                    }
                });

                try {
                    Log.d("VerifyEmailActivity", "Ready to send");

                    // 创建邮件内容
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(fromEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                    message.setSubject("Password Reset Verification Code");
                    message.setText("Your verification code is: " + verificationCode);

                    // 发送邮件
                    Transport.send(message);

                    // 邮件发送成功的日志输出
                    Log.d("VerifyEmailActivity", "Email sent successfully!");
                } catch (Exception e) {
                    Log.e("VerifyEmailActivity", "Failed to send code:", e);
                    e.printStackTrace();
                    // 确保UI线程提示用户错误
                    runOnUiThread(() -> {
                        Toast.makeText(VerifyEmailActivity.this, "Failed to send verification code.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

}

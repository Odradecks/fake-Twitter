package com.example.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import java.util.Date;

import java.util.Calendar;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText nameField, emailField, birthdayField;
    private Button nextButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createaccount);

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        TextView cancelTextView = findViewById(R.id.cancelTextView);
        cancelTextView.setOnClickListener(v -> finish());

        nameField = findViewById(R.id.nameInputField);
        emailField = findViewById(R.id.emailInputField);
        birthdayField = findViewById(R.id.birthInputField);
        nextButton = findViewById(R.id.nextButton);

        birthdayField.setFocusable(false);
        birthdayField.setClickable(true);
        birthdayField.setOnClickListener(v -> showDatePicker());

        nextButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String birthday = birthdayField.getText().toString().trim();
        // check integrity
        if (name.isEmpty() || email.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(CreateAccountActivity.this, "Please ensure that all information is filled in.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "This email address is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        // use firebase auth to sign up
        checkEmailAvailability();
    }

    private void showDatePicker() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 创建日期选择对话框
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateAccountActivity.this,
                R.style.CustomDatePickerDialogTheme,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 更新 EditText 内容为选择的日期
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    birthdayField.setText(formattedDate);
                },
                year, month, day);

        // 设置最大日期为当前日期
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        // 显示日期选择对话框
        datePickerDialog.show();
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }



    // check if the email is available
    private void checkEmailAvailability() {
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String birthday = birthdayField.getText().toString().trim();
        db.collection("User").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = false;
                        for (QueryDocumentSnapshot document: task.getResult()) {
                            emailExists = true;
                            break;
                        }
                        if (emailExists) {
                            Toast.makeText(this, "该邮箱已被注册", Toast.LENGTH_SHORT).show();
                        } else {
                            // 如果不存在该邮箱，进入下一步注册页面
                            Intent intent = new Intent(CreateAccountActivity.this, SetPasswordActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("name", name);
                            intent.putExtra("birthday", birthday);
                            startActivity(intent);
                        }
                    }
                    else {
                        Toast.makeText(this, "检查邮箱时出错：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

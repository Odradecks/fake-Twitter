package com.example.twitter;

import static com.example.twitter.CreateTweetActivity.STORAGE_PERMISSION_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private EditText usernameEditText, bioEditText, birthdayEditText;
    private TextView cancelTextView;
    private MaterialButton saveButton;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private OSSService ossService;
    private Uri avatarUri; // 用来暂存头像图片
    private String currentAvatarUrl; // 用来存储当前头像 URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);  // 布局文件

        // 初始化组件
        avatarImageView = findViewById(R.id.avatarImageView);
        usernameEditText = findViewById(R.id.usernameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        birthdayEditText = findViewById(R.id.birthdayEditText);
        cancelTextView = findViewById(R.id.cancelTextView);
        saveButton = findViewById(R.id.saveButton);

        // 初始化 Firestore 和 FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 初始化 OSS 服务
        ossService = new OSSService();
        ossService.initOSSClient(getApplicationContext());

        checkPermissions();

        // 加载当前用户信息
        loadUserProfile();

        // 取消按钮，返回到上一页
        cancelTextView.setOnClickListener(v -> finish());

        // 保存按钮，保存修改并更新 Firestore
        saveButton.setOnClickListener(v -> saveProfileInfo());

        // 头像点击事件，选择头像
        avatarImageView.setOnClickListener(v -> {
            // 打开图片选择器，用户选择头像
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 从 Firestore 加载用户信息
    private void loadUserProfile() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("User").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String bio = documentSnapshot.getString("bio");
                        String birthday = documentSnapshot.getString("birthday");
                        currentAvatarUrl = documentSnapshot.getString("avatar_url");

                        // 设置到对应的输入框
                        usernameEditText.setText(username);
                        bioEditText.setText(bio);
                        birthdayEditText.setText(birthday);

                        // 如果有头像 URL，加载头像图片
                        if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                            Glide.with(EditProfileActivity.this)
                                    .load(currentAvatarUrl)
                                    .into(avatarImageView);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to load user profile", Toast.LENGTH_SHORT).show());
    }

    // 保存用户资料到 Firestore
    private void saveProfileInfo() {
        String username = usernameEditText.getText().toString().trim();
        String bio = bioEditText.getText().toString().trim();
        String birthday = birthdayEditText.getText().toString().trim();

        // 验证数据是否为空
        if (username.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(EditProfileActivity.this, "Username and Birthday cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户的 UID
        String userId = auth.getCurrentUser().getUid();

        // 如果头像被修改，则上传图片并更新 URL
        if (avatarUri != null) {
            uploadAvatarToOSS(userId, avatarUri, new UploadCallback() {
                @Override
                public void onUploadSuccess(String imageUrl) {
                    // 更新 Firestore 中的用户数据
                    updateUserData(userId, username, bio, birthday, imageUrl);
                }

                @Override
                public void onUploadFailure(Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 如果头像没有修改，直接更新 Firestore 数据
            updateUserData(userId, username, bio, birthday, currentAvatarUrl);
        }
    }

    // 更新 Firestore 中的用户数据
    private void updateUserData(String userId, String username, String bio, String birthday, String avatarUrl) {
        // 创建更新数据的 Map
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("bio", bio);
        userData.put("birthday", birthday);
        userData.put("avatar_url", avatarUrl);

        db.collection("User").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    // 头像上传到阿里云 OSS
    private void uploadAvatarToOSS(String userId, Uri avatarUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                String filePath = getFilePathFromURI(avatarUri);
                if (filePath == null) {
                    callback.onUploadFailure(new Exception("Unable to get file path"));
                    return;
                }

                String objectKey = "avatars/" + userId + ".jpg"; // 用户头像在OSS中的存储路径
                String imageUrl = ossService.uploadImage(objectKey, filePath);

                if (imageUrl != null) {
                    callback.onUploadSuccess(imageUrl);
                } else {
                    callback.onUploadFailure(new Exception("Failed to upload image to OSS"));
                }
            } catch (Exception e) {
                callback.onUploadFailure(e);
            }
        }).start();
    }

    // 获取文件路径
    private String getFilePathFromURI(Uri uri) {
        String filePath = null;
        if (uri != null) {
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            }
        }
        return filePath;
    }

    // 上传回调接口
    interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(Exception e);
    }
}

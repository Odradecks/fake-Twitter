package com.example.twitter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.Date;

public class CreateTweetActivity extends AppCompatActivity {

    private static final String TAG = "CreateTweetActivity";
    static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private EditText tweetContentEditText;
    private ImageView selectedImageView;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private OSSService ossService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtweet);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tweetContentEditText = findViewById(R.id.tweetEditText);
        selectedImageView = findViewById(R.id.addImageButton);

        // 初始化OSS
        ossService = new OSSService();
        ossService.initOSSClient(getApplicationContext());

        // 检查并请求存储权限
        checkPermissions();

        findViewById(R.id.addImageButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        findViewById(R.id.postButton).setOnClickListener(v -> publishTweet());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
        }
    }

    private void publishTweet() {
        String content = tweetContentEditText.getText().toString();
        String UID = auth.getCurrentUser().getUid();

        if (content.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Cannot post empty tweet", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User").document(UID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String avatar_url = documentSnapshot.getString("avatar_url");
                        String email = documentSnapshot.getString("email");

                        // 创建 tweet 对象
                        String tweet_id = db.collection("Tweets").document().getId(); // 获取 tweetId
                        Tweet tweet = new Tweet(UID, content, "", new Timestamp(new Date()));  // 等待上传图片完成后设置 imageUrl
                        tweet.setTweetId(tweet_id);
                        tweet.setUsername(username);
                        tweet.setAvatarUrl(avatar_url);
                        tweet.setEmail(email);

                        if (imageUri != null) {
                            // 如果有图片，则上传图片并在成功上传后保存 tweet
                            uploadImageToOSS(tweet_id, new UploadCallback() {
                                @Override
                                public void onUploadSuccess(String imageUrl) {
                                    tweet.setImageUrl(imageUrl);
                                    saveTweetToFirestore(tweet);
                                    // 在成功保存 tweet 后，更新用户文档中的 tweets 数组
                                    addTweetToUserTweetsArray(UID, tweet_id);
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    showToast("Upload failed");
                                    Log.e(TAG, "Error uploading to OSS", e);
                                }
                            });
                        } else {
                            saveTweetToFirestore(tweet);
                            // 如果没有图片，直接保存 tweet，并更新用户文档中的 tweets 数组
                            addTweetToUserTweetsArray(UID, tweet_id);
                        }
                    } else {
                        showToast("User information not found");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to fetch user information");
                    Log.e(TAG, "Error fetching user information", e);
                });
    }

    private void addTweetToUserTweetsArray(String UID, String tweetId) {
        Log.d("add", tweetId);
        // 获取当前用户的文档，并在其中的 "tweets" 数组字段中添加 tweetId
        db.collection("User").document(UID)
                .update("tweets", FieldValue.arrayUnion(tweetId))  // 使用 FieldValue.arrayUnion() 添加 tweetId 到数组中
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Tweet ID added to user's tweets array"))
                .addOnFailureListener(e -> {
                    showToast("Failed to update user tweets array");
                    Log.e(TAG, "Error updating user tweets array", e);
                });
    }

    private void saveTweetToFirestore(Tweet tweet) {
        db.collection("Tweets").document(tweet.getTweetId())
                .set(tweet)
                .addOnSuccessListener(aVoid -> {
                    showToast("Tweet published successfully");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Failed to publish tweet"));
    }

    private void uploadImageToOSS(String tweetId, UploadCallback callback) {
        new Thread(() -> {
            try {
                String filePath = getFilePathFromURI(imageUri);  // 获得本地文件路径
                if (filePath == null) {
                    callback.onUploadFailure(new Exception("Unable to get file path"));
                    return;
                }

                String objectKey = "tweets/" + tweetId + ".jpg";  // 组织 OSS 文件路径，在OSS的bucket中以tweets作为目录，以tweetId命名
                String imageUrl = ossService.uploadImage(objectKey, filePath);  // 上传路径，本地路径
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

    private String getFilePathFromURI(Uri uri) {  // 获得本地文件的URI
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

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(CreateTweetActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(Exception e);
    }
}

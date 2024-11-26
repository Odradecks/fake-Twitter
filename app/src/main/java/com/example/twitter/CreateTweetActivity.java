package com.example.twitter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;

public class CreateTweetActivity extends AppCompatActivity {

    private static final String TAG = "CreateTweetActivity";
    private static final String SERVER_URL = "http://150.158.15.33/images/";
    private EditText tweetContentEditText;
    private ImageView selectedImageView;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    // 当创建一条tweet时，先会生成该条tweet的tweet_id，然后为其创建文档，自动填充其中的信息，然后将图片存入服务器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createtweet);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tweetContentEditText = findViewById(R.id.tweetEditText);
        selectedImageView = findViewById(R.id.addImageButton);

        findViewById(R.id.addImageButton).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });

        findViewById(R.id.postButton).setOnClickListener(v -> publishTweet());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
        }
    }

    private void publishTweet() {  // function
        String content = tweetContentEditText.getText().toString();
        String UID = auth.getCurrentUser().getUid();

        if (content.isEmpty() && imageUri == null) {
            Toast.makeText(this, "Cannot post empty tweet", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User").document(UID).get()
                .addOnSuccessListener(documentSnapShot -> {
                    if (documentSnapShot.exists()) {
                        String username = documentSnapShot.getString("username");
                        String avatar_url = documentSnapShot.getString("avatar_url");
                        String email = documentSnapShot.getString("email");

                        // create tweet object
                        String tweet_id = db.collection("Tweets").document().getId();
                        Tweet tweet = new Tweet(UID, content, "", new Timestamp(new Date()));  // tweet_id is not passed, and imageUrl is waiting to be completed
                        tweet.setTweetId(tweet_id);  // add tweet id
                        tweet.setUsername(username);
                        tweet.setAvatarUrl(avatar_url);
                        tweet.setEmail(email);

                        // save Tweet
                        if (imageUri != null) {
                            upLoadImage(tweet_id, new UploadCallback() {
                                @Override
                                public void onUploadSuccess(String imageUrl) {
                                    tweet.setImageUrl(imageUrl);
                                    saveTweetToFirestore(tweet);
                                }

                                @Override
                                public void onUploadFailure(Exception e) {
                                    Toast.makeText(CreateTweetActivity.this, "Upload photos failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            saveTweetToFirestore(tweet);
                        }
                    }
                    else {
                        Toast.makeText(this, "User information not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user information", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching user information", e);
                });
    }

    // how to generate a tweet id?
    private void saveTweetToFirestore(Tweet tweet) {
        db.collection("Tweets").document(tweet.getTweetId())  // path is empty here
                .set(tweet)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "发布失败", Toast.LENGTH_SHORT).show();
                });
    }

    private void upLoadImage(String tweetId, UploadCallback callback) {
        new Thread(() -> {
            try {
                File imageFile = new File(URI.create(imageUri.toString()));
                HttpURLConnection connection = (HttpURLConnection) new URL(SERVER_URL + tweetId + ".jpg").openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "image/jpeg");

                try (OutputStream os = new DataOutputStream(connection.getOutputStream());
                     FileInputStream fis = new FileInputStream(imageFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    callback.onUploadSuccess(SERVER_URL + tweetId + ".jpg");
                } else {
                    callback.onUploadFailure(new Exception("Server responded with: " + responseCode));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error uploading image", e);
                callback.onUploadFailure(e);
            }
        }).start();
    }

    interface UploadCallback {
        void onUploadSuccess(String imageUrl);
        void onUploadFailure(Exception e);
    }
}
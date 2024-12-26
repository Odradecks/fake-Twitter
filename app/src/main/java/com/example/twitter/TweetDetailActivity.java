package com.example.twitter;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class TweetDetailActivity extends AppCompatActivity {

    private static final String TAG = "TweetDetailActivity";

    private CircleImageView userAvatar;
    private TextView userName, userEmail, tweetTime, tweetContent;
    private ImageView tweetImage, moreOptions;
    private TextView commentCounter, retweetCounter, likeCounter, viewCounter;

    private FirebaseFirestore db;
    private String tweetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweetdetail);

        initializeViews();

        db = FirebaseFirestore.getInstance();
        tweetId = getIntent().getStringExtra("tweet_id");

        if (tweetId != null) {
            fetchTweetDetails(tweetId);
        } else {
            Log.e(TAG, "Tweet ID is null!");
        }
    }

    private void initializeViews() {
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        tweetTime = findViewById(R.id.tweetTime);
        tweetContent = findViewById(R.id.tweetContent);
        tweetImage = findViewById(R.id.tweetImage);
        moreOptions = findViewById(R.id.moreOptions);

        commentCounter = findViewById(R.id.commentCounter);
        retweetCounter = findViewById(R.id.retweetCounter);
        likeCounter = findViewById(R.id.likeCounter);
        viewCounter = findViewById(R.id.viewCounter);
    }

    private void fetchTweetDetails(String tweetId) {
        db.collection("Tweets").document(tweetId)
                .get()
                .addOnSuccessListener(tweetDoc -> {
                    if (tweetDoc.exists()) {
                        String content = tweetDoc.getString("content");
                        String imageUrl = tweetDoc.getString("image_url");
                        String uid = tweetDoc.getString("UID");
                        Timestamp timestamp = tweetDoc.getTimestamp("timestamp");

                        Long commentCount = tweetDoc.getLong("comment_count");
                        Long likeCount = tweetDoc.getLong("like_count");
                        Long viewCount = tweetDoc.getLong("view_count");
                        Long retweetCount = tweetDoc.getLong("retweet_count");

                        if (uid != null) {
                            renderTweetItem(uid, content, imageUrl, timestamp, commentCount, likeCount, viewCount, retweetCount);
                        } else {
                            Log.e(TAG, "UID is null for tweet ID: " + tweetId);
                        }
                    } else {
                        Log.e(TAG, "Tweet document not found for ID: " + tweetId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch tweet details", e));
    }

    private void renderTweetItem(String uid, String content, String imageUrl, Timestamp timestamp,
                                 Long commentCount, Long likeCount, Long viewCount, Long retweetCount) {
        db.collection("User").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        String username = userDoc.getString("username");
                        String email = userDoc.getString("email");
                        String avatarUrl = userDoc.getString("avatar_url");

                        userName.setText(username != null ? username : "Unknown User");
                        userEmail.setText(email != null ? email : "No Email");
                        tweetContent.setText(content != null ? content : "No Content");

                        likeCounter.setText(likeCount != null ? String.valueOf(likeCount) : "0");
                        commentCounter.setText(commentCount != null ? String.valueOf(commentCount) : "0");
                        viewCounter.setText(viewCount != null ? String.valueOf(viewCount) : "0");
                        retweetCounter.setText(retweetCount != null ? String.valueOf(retweetCount) : "0");

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                            String formattedDate = dateFormat.format(date);
                            tweetTime.setText(formattedDate); // 显示格式化后的时间
                        } else {
                            tweetTime.setText("Unknown Time");
                        }

                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Picasso.get()
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.default_avatar)
                                    .error(R.drawable.default_avatar)
                                    .into(userAvatar);
                        } else {
                            userAvatar.setImageResource(R.drawable.default_avatar);
                        }

                        // Load the tweet image if it's available
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            tweetImage.setVisibility(View.VISIBLE); // Show the ImageView
                            Glide.with(TweetDetailActivity.this)
                                    .load(imageUrl)
                                    .into(tweetImage);
                        } else {
                            tweetImage.setVisibility(View.GONE); // If no image, hide the ImageView
                        }

                        Log.d(TAG, "Tweet details rendered successfully.");
                    } else {
                        Log.e(TAG, "User document not found for UID: " + uid);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user details for UID: " + uid, e));
    }


}

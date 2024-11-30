package com.example.twitter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String profileUserId;

    private ImageView avatarImageView;
    private TextView usernameTextView, emailTextView, bioTextView, postsTextView;
    private Button followButton, unfollowButton, editProfileButton;
    private RecyclerView tweetsRecyclerView;

    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        avatarImageView = findViewById(R.id.profile_image);
        usernameTextView = findViewById(R.id.user_name);
        emailTextView = findViewById(R.id.user_email);
        bioTextView = findViewById(R.id.user_bio);
        postsTextView = findViewById(R.id.posts_placeholder);

        followButton = findViewById(R.id.follow_button);
        unfollowButton = findViewById(R.id.unfollow_button);
        editProfileButton = findViewById(R.id.edit_profile_button);

        tweetsRecyclerView = findViewById(R.id.tweets_recycler_view);
        if (tweetsRecyclerView == null) {
            Log.e("ProfileActivity", "tweetsRecyclerView is null!");
        } else {
            Log.d("ProfileActivity", "tweetsRecyclerView initialized successfully.");
        }
        tweetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tweetAdapter = new TweetAdapter(this, tweetList);
        tweetsRecyclerView.setAdapter(tweetAdapter);

        // 获取传递的 UID
        profileUserId = getIntent().getStringExtra("UID");

        // 加载用户信息和推文
        loadUserInfo();
        loadUserTweets();

        // 在 onCreate 方法中设置 EditProfile 按钮的点击事件
        editProfileButton.setOnClickListener(v -> {
            // 跳转到 EditProfileActivity
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("UID", profileUserId);  // 传递 UID
            startActivity(intent);
        });

        followButton.setOnClickListener(v -> {
            // 执行关注操作
            followUser();
            // 切换按钮状态
            followButton.setVisibility(View.GONE);
            unfollowButton.setVisibility(View.VISIBLE);
        });

        unfollowButton.setOnClickListener(v -> {
            // 执行取消关注操作
            unfollowUser();
            // 切换按钮状态
            unfollowButton.setVisibility(View.GONE);
            followButton.setVisibility(View.VISIBLE);
        });
    }

    private void followUser() {
        if (profileUserId == null || currentUserId == null) {
            Toast.makeText(this, "Invalid user information.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User").document(currentUserId)
                .update("following", FieldValue.arrayUnion(profileUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("User").document(profileUserId)
                            .update("followers", FieldValue.arrayUnion(currentUserId))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Followed successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ProfileActivity", "Error updating followers", e);
                                Toast.makeText(this, "Failed to follow user.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error updating following", e);
                    Toast.makeText(this, "Failed to follow user.", Toast.LENGTH_SHORT).show();
                });
    }

    private void unfollowUser() {
        if (profileUserId == null || currentUserId == null) {
            Toast.makeText(this, "Invalid user information.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User").document(currentUserId)
                .update("following", FieldValue.arrayRemove(profileUserId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("User").document(profileUserId)
                            .update("followers", FieldValue.arrayRemove(currentUserId))
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Unfollowed successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ProfileActivity", "Error updating followers", e);
                                Toast.makeText(this, "Failed to unfollow user.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error updating following", e);
                    Toast.makeText(this, "Failed to unfollow user.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserInfo() {
        db.collection("User").document(profileUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String avatarUrl = documentSnapshot.getString("avatar_url");
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String bio = documentSnapshot.getString("bio");

                        Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_avatar)
                                .into(avatarImageView);

                        usernameTextView.setText(username);
                        emailTextView.setText(email);
                        bioTextView.setText(bio);

                        // 根据 UID 判断显示按钮
                        boolean isFollowing = checkIfFollowing();  // 从数据库中查找该UID是否在当前登录用户的following列表内

                        if (profileUserId.equals(currentUserId)) {
                            editProfileButton.setVisibility(View.VISIBLE);
                            followButton.setVisibility(View.GONE);
                            unfollowButton.setVisibility(View.GONE);
                        }
                        else if (!isFollowing) {
                            editProfileButton.setVisibility(View.GONE);
                            followButton.setVisibility(View.VISIBLE);
                            unfollowButton.setVisibility(View.GONE);
                        }
                        else if (isFollowing) {
                            editProfileButton.setVisibility(View.GONE);
                            followButton.setVisibility(View.GONE);
                            unfollowButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error loading user info", e);
                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean checkIfFollowing() {
        // 查找profileUserId是否在currentUserId的following列表中
        // 使用同步返回值的逻辑需要调整为异步处理
        final boolean[] isFollowing = {false};
        db.collection("User").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> following = (List<String>) documentSnapshot.get("following");
                        if (following != null && following.contains(profileUserId)) {
                            isFollowing[0] = true;
                        }
                    }
                });
        return isFollowing[0]; // 注意：这里异步操作返回结果可能无法保证实时性

    }

    private void fetchTweets() {  // 获取到的用户信息要显示在tweet_item内
        tweetList.clear();
        tweetAdapter.notifyDataSetChanged();
        db.collection("Tweets")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String tweet_id = doc.getId();
                        fetchTweetDetails(tweet_id);
                    }
                });
    }
    private void fetchTweetDetails(String tweet_id) {
        db.collection("Tweets").document(tweet_id)
                .get()
                .addOnSuccessListener(tweetDoc -> {
                    String content = tweetDoc.getString("content");
                    String image_url = tweetDoc.getString("image_url");
                    String UID = tweetDoc.getString("UID");

                    com.google.firebase.Timestamp timestamp = tweetDoc.getTimestamp("timestamp");
                    Long comment_count = tweetDoc.getLong("comment_count");
                    Long like_count = tweetDoc.getLong("like_count");
                    Long view_count = tweetDoc.getLong("view_count");
                    Long retweet_count = tweetDoc.getLong("retweet_count");

                    renderTweetItem(UID, tweet_id, content, image_url, timestamp, comment_count, like_count, view_count, retweet_count);  // 用来渲染一张包含用户信息的tweet.
                });
    }
    // load user's tweets
    private void loadUserTweets() {
        tweetList.clear();
        db.collection("User").document(profileUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> tweets = (List<String>) documentSnapshot.get("tweets");
                        if (tweets == null || tweets.isEmpty()) {
                            String noPostsMessage = profileUserId.equals(currentUserId)
                                    ? "You have not posted yet."
                                    : "This user has not posted yet.";
                            postsTextView.setText(noPostsMessage);
                            postsTextView.setVisibility(View.VISIBLE);
                        } else {
                            postsTextView.setVisibility(View.GONE);

                            final int totalTweets = tweets.size();
                            final AtomicInteger loadedTweets = new AtomicInteger(0); // 用于计数加载的 tweets

                            for (String tweetId : tweets) {
                                Log.d("Print tweet ID", tweetId);
                                db.collection("Tweets").document(tweetId)
                                        .get()
                                        .addOnSuccessListener(tweetSnapshot -> {
                                            if (tweetSnapshot.exists()) {
                                                fetchTweetDetails(tweetId);
                                                if (loadedTweets.incrementAndGet() == totalTweets) {
                                                    tweetAdapter.notifyDataSetChanged(); // 所有 tweet 加载完成，刷新 UI
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("ProfileActivity", "Error loading tweet", e));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error loading tweets", e));
    }

    private void renderTweetItem(String UID, String tweet_id, String content, String image_url, com.google.firebase.Timestamp timestamp, Long comment_count, Long like_count, Long view_count, Long retweet_count) {
        db.collection("User").document(UID)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String username = userDoc.getString("username");
                    String email = userDoc.getString("email");
                    String avatar_url = userDoc.getString("avatar_url");  // test

                    userDoc.getData(); // 打印整个文档数据

                    Log.d("FirestoreData", "Username: " + username);
                    Log.d("FirestoreData", "Email: " + email);
                    Log.d("FirestoreData", "Avatar URL: " + avatar_url);

                    // 创建 Tweet 对象并添加到列表
                    Tweet tweet = new Tweet(tweet_id, content, image_url, UID, timestamp,
                            username, email, avatar_url, comment_count, retweet_count, like_count, view_count);
                    tweetList.add(tweet);
                    // 最后一次性刷新 RecyclerView
                    tweetAdapter.notifyDataSetChanged();
                });
    }
}

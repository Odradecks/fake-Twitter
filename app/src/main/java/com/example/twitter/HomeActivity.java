package com.example.twitter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TweetAdapter tweetAdapter;
    private List<Tweet> tweetList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private LinearLayout forYouTab;
    private LinearLayout followingTab;
    private View forYouIndicator;
    private View followingIndicator;
    private TextView forYouText;
    private TextView followingText;

    private ImageView avatarImageView; // （自己的）头像 ImageView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tweetAdapter = new TweetAdapter(this, tweetList);
        recyclerView.setAdapter(tweetAdapter);

        // 获取当前用户信息
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();


        // 获取 Firestore 实例
        db = FirebaseFirestore.getInstance();
        Log.d("User", "Fetching avatar for user: " + currentUserId);  // got
        avatarImageView = findViewById(R.id.avatar);

        // 监听 For You 推文列表
        fetchTweets();

        fetchUserAvatar();

        forYouTab = findViewById(R.id.forYouTab);
        followingTab = findViewById(R.id.followingTab);
        forYouIndicator = findViewById(R.id.forYouIndicator);
        followingIndicator = findViewById(R.id.followingIndicator);
        forYouText = findViewById(R.id.forYouText);
        followingText = findViewById(R.id.followingText);

        forYouIndicator.setVisibility(View.VISIBLE);
        followingIndicator.setVisibility(View.INVISIBLE);

        forYouTab.setOnClickListener(v -> {
            // 切换到 "For You" tab
            switchTab(true);
        });

        followingTab.setOnClickListener(v -> {
            // 切换到 "Following" tab
            switchTab(false);
        });

        ImageView fab = findViewById(R.id.fab_create_tweet);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateTweetActivity.class);
            startActivity(intent);
        });

    }
    private void fetchUserAvatar() {
        // 获取当前用户信息
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d("UserAvatar", "Fetching avatar for user: " + userId);  // got

            db.collectionGroup("User") // 可以使用 collectionGroup 获取所有子集
                    .get(Source.SERVER)
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // 打印所有 collection 的 ID
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d("Firestore", "Document ID: " + document.getId());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error getting collections: ", e);
                    });

            avatarImageView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                intent.putExtra("UID", currentUserId); // 传递当前用户 UID
                startActivity(intent);
            });


            // 从 Firestore 获取用户信息
            db.collection("User").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        // 获取 avatar_url 字段
                        String avatarUrl = userDoc.getString("avatar_url");  // null
                        Log.d("UserAvatar", "userId: " + userId);
                        String Username = userDoc.getString("Username");
                        Log.d("UserAvatar", "Username: " + Username);
                        Log.d("UserAvatar", "Avatar URL: " + avatarUrl);  // got url

                        // 使用 Glide 加载头像图片
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Log.d("UserAvatar", "Loading avatar image...");  // 输出加载头像的调试信息
                            Glide.with(HomeActivity.this)
                                    .load(avatarUrl)
                                    .circleCrop() // 圆形裁剪
                                    .placeholder(R.drawable.default_avatar) // 默认头像
                                    .into(avatarImageView)
                            ; // 设置头像
                        } else {
                            Log.d("UserAvatar", "Avatar URL is empty or null");  // 如果 avatar_url 为空，输出调试信息
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Failed to get user info", e);
                        Toast.makeText(HomeActivity.this, "Failed to load avatar", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("UserAvatar", "No user is logged in.");  // 如果当前没有登录用户，输出调试信息
        }
    }

    private void switchTab(boolean isForYouTabSelected) {
        if (isForYouTabSelected) {
            // 显示 "For You" tab 的内容
            forYouIndicator.setVisibility(View.VISIBLE);
            followingIndicator.setVisibility(View.INVISIBLE);
            forYouText.setTextColor(Color.BLACK);
            followingText.setTextColor(Color.GRAY);

            // 加载 "For You" 推文
            fetchTweets();
        } else {
            // 显示 "Following" tab 的内容
            forYouIndicator.setVisibility(View.INVISIBLE);
            followingIndicator.setVisibility(View.VISIBLE);
            forYouText.setTextColor(Color.GRAY);
            followingText.setTextColor(Color.BLACK);

            // 加载 "Following" 推文
            fetchFollowingList();
        }
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
                    Log.d("HomeActivity", "image_url" + image_url);
                    String UID = tweetDoc.getString("UID");

                    com.google.firebase.Timestamp timestamp = tweetDoc.getTimestamp("timestamp");
                    Long comment_count = tweetDoc.getLong("comment_count");
                    Long like_count = tweetDoc.getLong("like_count");
                    Long view_count = tweetDoc.getLong("view_count");
                    Long retweet_count = tweetDoc.getLong("retweet_count");

                    renderTweetItem(UID, tweet_id, content, image_url, timestamp, comment_count, like_count, view_count, retweet_count);  // 用来渲染一张包含用户信息的tweet.
                });
    }
    private void fetchFollowingList() {  // get current user's following list first
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String UID = user.getUid();
            db.collection("User").document(UID)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        // 获取following数组
                        List<String> followingList = (List<String>) userDoc.get("following");
                        if (followingList != null) {
                            // 加载当前用户关注的用户的推文
                            fetchFollowingTweets(followingList);
                        }
                        else {
                            Log.d("Following", "No following found.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Following", "Error fetching following list", e);
                    });
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private void fetchFollowingTweets(List<String> followingList) {
        tweetList.clear();
        tweetAdapter.notifyDataSetChanged();

        db.collection("Tweets")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String tweet_id = doc.getId();
                        String tweet_uid = doc.getString("UID");

                        if (followingList.contains(tweet_uid)) {
                            // check if the UID of this tweet is contained in the current user's following list.
                            fetchTweetDetails(tweet_id);
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Tweets", "Error fetching tweets", e);
                });
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

                    tweetAdapter.setOnAvatarClickListener((clickedUID) -> {  //
                        Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                        intent.putExtra("UID", clickedUID);  // 传递 UID
                        startActivity(intent);
                    });
                    // 最后一次性刷新 RecyclerView
                    tweetAdapter.notifyDataSetChanged();
                });
    }

    public void onAvatarClick(String clickedUID) {
        // 在这里处理头像点击事件，例如跳转到用户详情页
        Log.d("AvatarClick", "Clicked UID: " + clickedUID);
        // 你可以根据点击的 UID 做相应的操作，比如跳转到用户主页
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("UID", clickedUID);
        startActivity(intent);
    }
}

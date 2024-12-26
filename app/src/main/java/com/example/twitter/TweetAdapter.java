package com.example.twitter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    private final Context context;
    private final List<Tweet> tweetList;
    private OnAvatarClickListener onAvatarClickListener;

    // 定义接口，要点击tweet_item的头像就可以获得该用户的UID
    public interface OnAvatarClickListener {
        void onAvatarClick(String clickedUID);  // 接收点击的用户 UID
    }

    public void setOnAvatarClickListener(OnAvatarClickListener listener) {
        this.onAvatarClickListener = listener;
    }

    public TweetAdapter(Context context, List<Tweet> tweetList) {  // 构造函数，接受一个Context访问应用内的资源和文件，一个List<Tweet>类型的集合
        this.context = context;
        this.tweetList = tweetList;
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  // 创建一个新的TweetViewHolder对象
        View view = LayoutInflater.from(context).inflate(R.layout.tweet_item, parent, false);  // 加载布局tweet_item.xml并创建视图
        return new TweetViewHolder(view);  // view是一个单个推文项的视图布局
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {  // 在每次RecyclerView项被绑定时调用，负责将数据从tweetList中取出并填充到TweetViewHolder的视图组建中
        Tweet tweet = tweetList.get(position);  // 获得对应位置的推文

        // 设置头像点击事件，传递用户 UID
        holder.userAvatar.setOnClickListener(v -> {
            if (onAvatarClickListener != null) {
                String userUID = tweet.getUID(); // 从 tweet 获取用户的 UID
                onAvatarClickListener.onAvatarClick(userUID); // 调用回调方法传递 UID
            }
        });

        holder.itemView.setTag(tweet.getTweetId());
        holder.itemView.setOnClickListener(v -> {
            String tweetId = tweet.getTweetId();  // 获取当前推文的 ID
            Log.d("TweetClick", "Clicked Tweet ID: " + tweetId);

            // 跳转到 TweetDetailActivity 并传递 tweet_id
            Intent intent = new Intent(context, TweetDetailActivity.class);
            intent.putExtra("tweet_id", tweetId); // 使用键 "tweet_id" 传递数据
            context.startActivity(intent); // 启动 TweetDetailActivity
        });

        String tweet_id = tweet.getTweetId();

        Log.d("TweetClick", "Clicked Tweet ID: " + tweet_id);
        // 加载用户头像
        Picasso.get().load(tweet.getAvatarUrl()).placeholder(R.drawable.default_avatar).into(holder.userAvatar);  // Picasso加载用户头像，placeholder加载默认头像

        // 设置用户信息
        holder.userName.setText(tweet.getUsername());
        holder.userEmail.setText(tweet.getEmail());
        com.google.firebase.Timestamp firebaseTimestamp = tweet.getTimestamp(); // 获取 Firebase Timestamp
        if (firebaseTimestamp != null) {
            Date date = firebaseTimestamp.toDate(); // 转换为 java.util.Date
            String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
            holder.tweetTime.setText(formattedTime);
        } else {
            holder.tweetTime.setText("N/A");
        }

        // 设置推文内容
        holder.tweetContent.setText(tweet.getContent());  // 填充推文内容
        Log.d("test", "test");

        // 设置推文图片（可选）
        if (tweet.getImageUrl() != null && !tweet.getImageUrl().isEmpty()) {
            Log.d("Tweet id", tweet_id);
            Log.d("image_url", tweet.getImageUrl());
            holder.tweetImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(tweet.getImageUrl())
                    .into(holder.tweetImageView);
        } else {
            holder.tweetImageView.setVisibility(View.GONE); // 如果没有图片，隐藏 ImageView
        }

        // 设置评论、转发、点赞、浏览计数
        holder.commentCounter.setText(String.valueOf(tweet.getCommentCount()));  // 填充其他控件
        holder.retweetCounter.setText(String.valueOf(tweet.getRetweetCount()));
        holder.likeCounter.setText(String.valueOf(tweet.getLikeCount()));
        holder.viewCounter.setText(String.valueOf(tweet.getViewCount()));

        setButtonState(holder, tweet);

        // 设置点赞、评论、转发的点击事件
        holder.likeIcon.setOnClickListener(v -> updateTweetCount(tweet, "like_count"));
        holder.retweetIcon.setOnClickListener(v -> updateTweetCount(tweet, "retweet_count"));
    }

    private void setButtonState(TweetViewHolder holder, Tweet tweet) {
        // 设置点赞按钮的状态
        if (tweet.isLiked()) {
            holder.likeIcon.setImageResource(R.drawable.ic_liked);
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like);
        }

        // 设置转发按钮的状态
        if (tweet.isRetweeted()) {
            holder.retweetIcon.setImageResource(R.drawable.ic_retweeted);
        } else {
            holder.retweetIcon.setImageResource(R.drawable.ic_retweet);
        }

    }

    private void updateTweetCount(Tweet tweet, String field) {
        long currentCount = 0;
        boolean isLiked = tweet.isLiked();
        boolean isRetweeted = tweet.isRetweeted();
        boolean isCommented = tweet.isCommented();
        // 只能点赞一次或取消，并且要全局共享。能不能设置一个(tweet_id, is_liked)
        switch (field) {
            case "like_count":
                currentCount = tweet.getLikeCount();
                if (isLiked) {
                    tweet.setLiked(false);
                    currentCount--;  // Decrease like count
                } else {
                    tweet.setLiked(true);
                    currentCount++;  // Increase like count
                }
                break;
            case "retweet_count":
                currentCount = tweet.getRetweetCount();
                if (isRetweeted) {
                    tweet.setRetweeted(false);
                    currentCount--;  // Decrease retweet count
                } else {
                    tweet.setRetweeted(true);
                    currentCount++;  // Increase retweet count
                }
                break;
        }

        // update data in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long finalCurrentCount = currentCount;
        String tweet_id = tweet.getTweetId();  //
        db.collection("Tweets").document(tweet.getTweetId())
                .update(field, currentCount)  // 这里直接使用currentCount，不加1
                .addOnSuccessListener(aVoid -> {
                    Log.d("Tweet ID", tweet_id);
                    // update local data
                    tweet.updateCount(field, finalCurrentCount);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("TweetAdapter", "Error updating tweet count", e);
                });
    }


    @Override
    public int getItemCount() {
        return tweetList.size();
    }



    public static class TweetViewHolder extends RecyclerView.ViewHolder {
        public ImageView tweetImageView = itemView.findViewById(R.id.tweetImage);  // 一个ViewHolder类，用来存储每一项推文中的控件引用，避免每次绑定时都进行查找
        ImageView userAvatar, tweetImage, commentIcon, retweetIcon, likeIcon, viewIcon, saveIcon, shareIcon;  // 定义控件
        TextView userName, userEmail, tweetTime, tweetContent, commentCounter, retweetCounter, likeCounter, viewCounter;

        @SuppressLint("WrongViewCast")
        public TweetViewHolder(@NonNull View itemView) {  // 初始化视图，用tweet_item初始化了，找到布局中控件并引用
            super(itemView);
            // 初始化视图
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            tweetTime = itemView.findViewById(R.id.tweetTime);
            tweetContent = itemView.findViewById(R.id.tweetContent);
            tweetImage = itemView.findViewById(R.id.tweetImage);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            retweetIcon = itemView.findViewById(R.id.retweetIcon);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            viewIcon = itemView.findViewById(R.id.viewIcon);
            saveIcon = itemView.findViewById(R.id.saveIcon);
            shareIcon = itemView.findViewById(R.id.shareIcon);
            commentCounter = itemView.findViewById(R.id.commentCounter);
            retweetCounter = itemView.findViewById(R.id.retweetCounter);
            likeCounter = itemView.findViewById(R.id.likeCounter);
            viewCounter = itemView.findViewById(R.id.viewCounter);
        }
    }
}

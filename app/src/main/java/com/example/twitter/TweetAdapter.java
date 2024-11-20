package com.example.twitter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    private final Context context;
    private final List<Tweet> tweetList;

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
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {  // 在每次RecyclerView香被绑定时调用，负责将数据从tweetList中取出并填充到TweetViewHolder的视图组建中
        Tweet tweet = tweetList.get(position);

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

        // 设置推文图片（可选）
        if (tweet.getImageUrl() != null && !tweet.getImageUrl().isEmpty()) {
            holder.tweetImage.setVisibility(View.VISIBLE);
            Picasso.get().load(tweet.getImageUrl()).into(holder.tweetImage);
        } else {
            holder.tweetImage.setVisibility(View.GONE);
        }

        // 设置评论、转发、点赞、浏览计数
        holder.commentCounter.setText(String.valueOf(tweet.getCommentCount()));  // 填充其他控件
        holder.retweetCounter.setText(String.valueOf(tweet.getRetweetCount()));
        holder.likeCounter.setText(String.valueOf(tweet.getLikeCount()));
        holder.viewCounter.setText(String.valueOf(tweet.getViewCount()));
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    public static class TweetViewHolder extends RecyclerView.ViewHolder {  // 一个ViewHolder类，用来存储每一项推文中的控件引用，避免每次绑定时都进行查找
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

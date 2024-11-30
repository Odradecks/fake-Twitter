package com.example.twitter;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class Tweet {
    @PropertyName("tweet_id")
    private String tweet_id;
    private String content;
    @PropertyName("image_url")
    private String image_url;
    @PropertyName("UID")
    private String UID;
    private com.google.firebase.Timestamp timestamp;
    private String username;
    private String email;
    @PropertyName("avatar_url")
    private String avatar_url;
    @PropertyName("comment_count")
    private Long comment_count = 0L;
    @PropertyName("retweet_count")
    private Long retweet_count = 0L;
    @PropertyName("like_count")
    private Long like_count = 0L;
    @PropertyName("view_count")
    private Long view_count = 0L;

    private boolean isLiked;
    private boolean isRetweeted;
    private boolean isCommented;
    // 默认构造函数
    public Tweet() {}

    // 带参数的构造函数
    public Tweet(String tweet_id, String content, String image_url, String UID, com.google.firebase.Timestamp timestamp,
                 String username, String email, String avatar_url, Long comment_count, Long retweet_count,
                 Long like_count, Long view_count) {
        this.tweet_id = tweet_id;
        this.content = content;
        this.image_url = image_url;

        this.UID = UID;
        this.timestamp = timestamp;
        this.username = username;
        this.email = email;
        this.avatar_url = avatar_url;
        this.comment_count = comment_count;
        this.retweet_count = retweet_count;
        this.like_count = like_count;
        this.view_count = view_count;
    }

    public Tweet(String userId, String content, String imageUrl, Timestamp timestamp) {
        this.tweet_id = null; // Will be set during saving
        this.UID = userId;
        this.content = content;
        this.image_url = imageUrl;
        this.timestamp = timestamp;
    }


    // Getter and Setter methods for each field
    @PropertyName("tweet_id")
    public String getTweetId() {
        return tweet_id;
    }

    @PropertyName("tweet_id")
    public void setTweetId(String tweet_id) {
        this.tweet_id = tweet_id;
    }

    @PropertyName("UID")
    public String getUID() {
        return UID;
    }

    @PropertyName("UID")
    public void setUID(String UID) {
        this.UID = UID;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return image_url;
    }

    @PropertyName("image_url")
    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("avatar_url")
    public String getAvatarUrl() {
        return avatar_url;
    }
    @PropertyName("avatar_url")
    public void setAvatarUrl(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    @PropertyName("comment_count")
    public Long getCommentCount() {
        return comment_count;
    }
    @PropertyName("comment_count")
    public void setCommentCount(Long comment_count) {
        this.comment_count = comment_count;
    }
    @PropertyName("retweet_count")
    public Long getRetweetCount() {
        return retweet_count;
    }
    @PropertyName("retweet_count")
    public void setRetweetCount(Long retweet_count) {
        this.retweet_count = retweet_count;
    }
    @PropertyName("like_count")
    public Long getLikeCount() {
        return like_count;
    }
    @PropertyName("like_count")
    public void setLikeCount(Long like_count) {
        this.like_count = like_count;
    }
    @PropertyName("view_count")
    public Long getViewCount() {
        return view_count;
    }
    @PropertyName("view_count")
    public void setViewCount(Long view_count) {
        this.view_count = view_count;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isRetweeted() {
        return isRetweeted;
    }

    public void setRetweeted(boolean retweeted) {
        isRetweeted = retweeted;
    }

    public boolean isCommented() {
        return isCommented;
    }

    public void setCommented(boolean commented) {
        isCommented = commented;
    }
    public void updateCount(String field, long newCount) {
        switch (field) {
            case "like_count":
                this.like_count = newCount;
                break;
            case "comment_count":
                this.comment_count = newCount;
                break;
            case "retweet_count":
                this.retweet_count = newCount;
                break;
        }
    }
}

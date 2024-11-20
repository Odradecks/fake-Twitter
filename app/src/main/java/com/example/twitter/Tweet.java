package com.example.twitter;

public class Tweet {
    private String tweet_id;
    private String content;
    private String image_url;
    private String UID;
    private com.google.firebase.Timestamp timestamp;
    private String username;
    private String email;
    private String avatar_url;
    private Long comment_count;
    private Long retweet_count;
    private Long like_count;
    private Long view_count;

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


    // Getter and Setter methods for each field
    public String getTweetId() {
        return tweet_id;
    }

    public void setTweetId(String tweet_id) {
        this.tweet_id = tweet_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
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

    public String getAvatarUrl() {
        return avatar_url;
    }

    public void setAvatarUrl(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public Long getCommentCount() {
        return comment_count;
    }

    public void setCommentCount(Long comment_count) {
        this.comment_count = comment_count;
    }

    public Long getRetweetCount() {
        return retweet_count;
    }

    public void setRetweetCount(Long retweet_count) {
        this.retweet_count = retweet_count;
    }

    public Long getLikeCount() {
        return like_count;
    }

    public void setLikeCount(Long like_count) {
        this.like_count = like_count;
    }

    public Long getViewCount() {
        return view_count;
    }

    public void setViewCount(Long view_count) {
        this.view_count = view_count;
    }
}

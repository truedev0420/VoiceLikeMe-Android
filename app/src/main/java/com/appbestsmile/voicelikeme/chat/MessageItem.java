package com.appbestsmile.voicelikeme.chat;

import java.util.List;

public class MessageItem {

    public String documentId;
    public String userId;
    public String userNickname;
    public String userAvatar;
    public String userMessageNickname;
    public String message;
    public String timestamp;
    public String mediaPath;
    public List<String> likedUsers;

    public boolean isFirstSent = false;

    public MessageItem(String documentId, String userId, String userNickname, String userAvatar, String userMessageNickname, String message, String timestamp, String mediaPath, List<String> likedUsers){

        this.documentId = documentId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.userAvatar = userAvatar;
        this.userMessageNickname = userMessageNickname;
        this.message = message;
        this.timestamp = timestamp;
        this.mediaPath = mediaPath;
        isFirstSent = false;
        this.likedUsers = likedUsers;
    }

    public MessageItem(String timestamp)
    {
        this.timestamp = timestamp;
        this.isFirstSent = true;
    }
}

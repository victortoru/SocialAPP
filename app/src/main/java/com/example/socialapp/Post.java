package com.example.socialapp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    public String uid;
    public String author;
    public String authorPhotoUrl;
    public String content;
    public String mediaUrl;
    public String mediaType;
    public Date date;

    // Constructor vacio requerido por Firestore
    public Post() {}
    public Map<String, Boolean> likes = new HashMap<>();

    public Post(String uid, String author, String authorPhotoUrl, String content, String mediaUrl, String mediaType, Date date) {
        this.uid = uid;
        this.author = author;
        this.authorPhotoUrl = authorPhotoUrl;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
        this.date=date;
    }
}

package com.example.hikernotes.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by John on 8/16/2016.
 */
public class Tour extends RealmObject {
    @PrimaryKey
    private int id;
    private String title;
    private String date;
    private int likes;
    private String thumb_img_ref;

    public Tour() {
    }

    public Tour(int id, String title, String date, int likes, String thumb_img_ref) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.likes = likes;
        this.thumb_img_ref = thumb_img_ref;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getThumb_img_ref() {
        return thumb_img_ref;
    }

    public void setThumb_img_ref(String thumb_img_ref) {
        this.thumb_img_ref = thumb_img_ref;
    }
}

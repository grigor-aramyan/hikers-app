package com.example.hikernotes.realms;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by John on 8/16/2016.
 */
public class Tour extends RealmObject {
    @PrimaryKey
    private int id;
    private String author;
    private String title;
    private Date date;
    private String info;
    private int likes;
    private String img_references_str;
    private String trail;
    private String onMapImages;

    public Tour() {
    }

    public Tour(int id, String author, String title, Date date, String info, int likes, String img_references_str, String trail, String onMapImages) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.date = date;
        this.info = info;
        this.likes = likes;
        this.img_references_str = img_references_str;
        this.trail = trail;
        this.onMapImages = onMapImages;
    }

    public String getOnMapImages() {
        return onMapImages;
    }

    public void setOnMapImages(String onMapImages) {
        this.onMapImages = onMapImages;
    }

    public int getId() {
        return id;
    }

    public String getTrail() {
        return trail;
    }

    public void setTrail(String trail) {
        this.trail = trail;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getImg_references_str() {
        return img_references_str;
    }

    public void setImg_references_str(String img_references_str) {
        this.img_references_str = img_references_str;
    }
}

package com.example.hikernotes.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by John on 8/17/2016.
 */
public class CurrentTour extends RealmObject {
    @PrimaryKey
    private int id;
    private String title, author, date, info, tour_imgs_refs;

    public CurrentTour() {
        this.id = 10001;
    }

    public CurrentTour(String title, String author, String date, String info, String tour_imgs_refs) {
        this.id = 10001;
        this.title = title;
        this.author = author;
        this.date = date;
        this.info = info;
        this.tour_imgs_refs = tour_imgs_refs;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getTour_imgs_refs() {
        return tour_imgs_refs;
    }

    public void setTour_imgs_refs(String tour_imgs_refs) {
        this.tour_imgs_refs = tour_imgs_refs;
    }
}

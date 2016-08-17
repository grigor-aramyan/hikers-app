package com.example.hikernotes.realms;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by John on 8/16/2016.
 */
public class SavedTrail extends RealmObject {
    @PrimaryKey
    private int id;
    private String tour_name;
    private String trail;

    public SavedTrail() {
    }

    public SavedTrail(int id, String tour_name, String trail) {
        this.id = id;
        this.tour_name = tour_name;
        this.trail = trail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTour_name() {
        return tour_name;
    }

    public void setTour_name(String tour_name) {
        this.tour_name = tour_name;
    }

    public String getTrail() {
        return trail;
    }

    public void setTrail(String trail) {
        this.trail = trail;
    }
}

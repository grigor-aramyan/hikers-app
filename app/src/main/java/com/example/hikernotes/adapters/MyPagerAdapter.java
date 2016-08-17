package com.example.hikernotes.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.fragments.RecyclerFragment;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by John on 8/16/2016.
 */
public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private int mCurrent_position;

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        mCurrent_position = position;
        Bundle bundle = new Bundle();
        bundle.putInt("page", position);
        RecyclerFragment fragment = new RecyclerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Tour> realmResults = realm.where(Tour.class).findAll();
        if (realmResults.size() > 0) {
            int page_count = (int) Math.floor(realmResults.size() / 10 + 1);
            return page_count;
        } else {
            return 0;
        }
    }

    public Fragment getUpdatedItem() {
        return getItem(mCurrent_position);
    }
}

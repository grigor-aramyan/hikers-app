package com.example.hikernotes.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dgreenhalgh.android.simpleitemdecoration.linear.DividerItemDecoration;
import com.example.hikernotes.R;
import com.example.hikernotes.adapters.MainRecyclerListAdapter;
import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.utils.EndlessRecyclerViewScrollListener;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by John on 8/16/2016.
 */
public class RecyclerFragment extends Fragment {
    //    private int page_number;
    private RealmList<Tour> mToursList;
    private int SORT_FLAG = 1;
    private MainRecyclerListAdapter mListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_list, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_list);
//        page_number = getArguments().getInt("page", 0);
        mToursList = new RealmList<>();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("sort_type", Context.MODE_PRIVATE);
        SORT_FLAG = sharedPreferences.getInt("sort_by", 1);

        mListAdapter = new MainRecyclerListAdapter(mToursList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMore(page);
            }
        });


        /*Drawable divider_drawable = ContextCompat.getDrawable(getActivity(), R.drawable.list_decor);
        recyclerView.addItemDecoration(new DividerItemDecoration(divider_drawable));*/
        recyclerView.setAdapter(mListAdapter);
        getTours(mToursList, 0, SORT_FLAG);
        return view;
    }

    void loadMore(int page) {
        if (page == 0) {
            mToursList.clear();
        }

        getTours(mToursList, page, SORT_FLAG);
        mListAdapter.notifyDataSetChanged();

    }

    void getTours(RealmList<Tour> tours, int page_number, int sort_flag) {
        int start_id = page_number * 10 + 1;
        int end_id;
        if (start_id == 1) {
            end_id = 10;
        } else {
            end_id = start_id + 9;
        }
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Tour> realmResults = realm.where(Tour.class).between("id", start_id, end_id).findAll();
        switch (sort_flag) {
            case 1:

                realmResults.sort("date", Sort.DESCENDING);

                break;
            case 2:

                realmResults.sort("likes", Sort.DESCENDING);

                break;
            default:
                break;
        }

        System.out.println("*********************start_id = " + start_id);
        System.out.println("*********************end_id = " + end_id);

        tours.addAll(realmResults); // pochic avelacnum enq load exacner@

    }

}

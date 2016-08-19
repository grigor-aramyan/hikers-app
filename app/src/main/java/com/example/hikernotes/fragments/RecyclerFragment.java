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

/**
 * Created by John on 8/16/2016.
 */
public class RecyclerFragment extends Fragment {
    private int page_number;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_list, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_list);
        page_number = getArguments().getInt("page", 0);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("sort_type", Context.MODE_PRIVATE);
        int sort_flag = sharedPreferences.getInt("sort_by", 1);

        MainRecyclerListAdapter listAdapter = new MainRecyclerListAdapter(page_number, sort_flag);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        /*Drawable divider_drawable = ContextCompat.getDrawable(getActivity(), R.drawable.list_decor);
        recyclerView.addItemDecoration(new DividerItemDecoration(divider_drawable));*/
        recyclerView.setAdapter(listAdapter);

        return view;
    }
}

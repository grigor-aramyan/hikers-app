package com.example.hikernotes.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.example.hikernotes.R;
import com.example.hikernotes.activities.DetailsActivity;
import com.example.hikernotes.adapters.CommentsListAdapter;

/**
 * Created by John on 8/18/2016.
 */
public class ShowCommentsBlock extends FrameLayout {
    private RecyclerView mCommentsList;
    public static int mSelectedTourID;

    public ShowCommentsBlock(Context context) {
        super(context);
        //this.mSelectedTourID = DetailsActivity.mSelectedTourID;
        initViews();
    }

    public ShowCommentsBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.mSelectedTourID = DetailsActivity.mSelectedTourID;
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.block_show_comments, this, true);
        mCommentsList = (RecyclerView) findViewById(R.id.comments_list);
        Log.e("yyy", "Tour id: " + mSelectedTourID);
        CommentsListAdapter adapter = new CommentsListAdapter(mSelectedTourID, getContext());

        mCommentsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mCommentsList.setAdapter(adapter);
    }

}

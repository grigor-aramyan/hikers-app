package com.example.hikernotes.widgets;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.MainActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.activities.DetailsActivity;
import com.example.hikernotes.adapters.CommentsListAdapter;
import com.example.hikernotes.consumptions.VolleyRequests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 8/18/2016.
 */
public class ShowCommentsBlock extends FrameLayout {
    private RecyclerView mCommentsList;
    private int mSelectedTourID;
    private RequestQueue mQueue;
    private CommentsListAdapter mAdapter;

    public ShowCommentsBlock(Context context) {
        super(context);
        this.mSelectedTourID = DetailsActivity.mSelectedTourID;
        initViews();
    }

    public ShowCommentsBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSelectedTourID = DetailsActivity.mSelectedTourID;
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.block_show_comments, this, true);
        mCommentsList = (RecyclerView) findViewById(R.id.comments_list);

        mQueue = Volley.newRequestQueue(getContext());

        fetchAndDisplayComments();

    }

    // ToDo: remove data fetching chunk from view
    public void fetchAndDisplayComments() {
        StringRequest request = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForConnectivityCheck, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.startsWith("got it")) {
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForPullingComments, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.startsWith("no comments")) {
                                // no comments for this tour
                                Toast.makeText(getContext(), "No comments for this tour!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] comments_str = response.split(":::");
                            String[] tmp;
                            ArrayList<String> authors = new ArrayList<>();
                            ArrayList<String> comments = new ArrayList<>();
                            for (int i = 0; i < (comments_str.length - 1); i++) {
                                String s = comments_str[i];
                                tmp = s.split("--");
                                authors.add(tmp[0]);
                                comments.add(tmp[1]);
                            }

                            mAdapter = new CommentsListAdapter(authors, comments, getContext());

                            mCommentsList.setLayoutManager(new LinearLayoutManager(getContext()));
                            mCommentsList.setAdapter(mAdapter);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getContext(), "Something wrong with retrieving comments! Sorry!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("tourid", mSelectedTourID + "");
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                    mQueue.add(stringRequest);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Check your net access, please!!", Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(request);
    }

}

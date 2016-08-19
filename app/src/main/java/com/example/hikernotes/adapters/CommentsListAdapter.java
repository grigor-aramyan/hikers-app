package com.example.hikernotes.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.MainActivity;
import com.example.hikernotes.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 8/18/2016.
 */
public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.CommentViewHolder> {
    private RequestQueue mQueue;
    private int mSelectedID;
    private ArrayList<String> authors, comments;

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        public TextView name_txt, comment_txt;

        public CommentViewHolder(View itemView) {
            super(itemView);

            this.name_txt = (TextView) itemView.findViewById(R.id.name_field_id);
            this.comment_txt = (TextView) itemView.findViewById(R.id.comment_field_id);
        }
    }

    public CommentsListAdapter(final int tour_id, final Context context) {
        mQueue = Volley.newRequestQueue(context);
        authors = new ArrayList<>();
        comments = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.POST, MainActivity.sUrlForConnectivityCheck, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.startsWith("got it")) {
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.sUrlForPullingComments, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.startsWith("no comments")) {
                                // no comments for this tour
                                Toast.makeText(context, "No comments for this tour!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            String[] comments_str = response.split(":::");
                            String[] tmp;
                            for (int i = 0; i < (comments_str.length - 1); i++) {
                                String s = comments_str[i];
                                tmp = s.split("--");
                                authors.add(tmp[0]);
                                comments.add(tmp[1]);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "Something wrong with retrieving comments! Sorry!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("tourid", tour_id + "");
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
                Toast.makeText(context, "Check your net access, please!!", Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(request);
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View list_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_row, parent, false);
        return new CommentViewHolder(list_row);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Log.e("yyy", "Position: " + position);
        int real_position = comments.size() - position - 1;
        holder.name_txt.setText(authors.get(real_position));
        holder.comment_txt.setText(comments.get(real_position));
        Log.e("yyy", "comments size: " + comments.size());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}

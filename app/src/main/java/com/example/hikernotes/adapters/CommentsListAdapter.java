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

    public CommentsListAdapter(ArrayList<String> authors, ArrayList<String> comments, final Context context) {
        this.authors = authors;
        this.comments = comments;

    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View list_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_row, parent, false);
        return new CommentViewHolder(list_row);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        int real_position = comments.size() - position - 1;

        holder.name_txt.setText(authors.get(real_position));
        holder.comment_txt.setText(comments.get(real_position));


    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

}

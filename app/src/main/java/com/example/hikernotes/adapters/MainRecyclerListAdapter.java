package com.example.hikernotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.R;
import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.activities.DetailsActivity;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by John on 8/16/2016.
 */
public class MainRecyclerListAdapter extends RecyclerView.Adapter<MainRecyclerListAdapter.MyViewHolder> {
    private RealmList<Tour> tours;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mTitle;
        public TextView mDate;
        public TextView mLikes;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.mImageView = (ImageView) itemView.findViewById(R.id.item_image_id);
            this.mTitle = (TextView) itemView.findViewById(R.id.item_title_id);
            this.mDate = (TextView) itemView.findViewById(R.id.item_date_id);
            this.mLikes = (TextView) itemView.findViewById(R.id.item_likes_id);
        }
    }

    public MainRecyclerListAdapter(int page_number, int sort_flag) {
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
        tours = new RealmList<>();
        for (Tour tour: realmResults) {
            tours.add(tour);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View list_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_main_list, parent, false);
        return new MyViewHolder(list_row);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Tour tour = tours.get(position);
        holder.mTitle.setText(tour.getTitle());
        holder.mDate.setText(tour.getDate());
        holder.mLikes.setText(tour.getLikes() + "");
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra("selected-tour-id", tour.getId());
                mContext.startActivity(intent);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(mContext);
        ImageRequest imageRequest = new ImageRequest(tour.getThumb_img_ref(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                holder.mImageView.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(imageRequest);

    }

    @Override
    public int getItemCount() {
        return tours.size();
    }
}

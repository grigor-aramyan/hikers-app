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
import com.example.hikernotes.utils.MeasureUnitConversionUtils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

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
    private int thumb_size_in_px = 1;

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

        int start_index, end_index;
        start_index = page_number * 10;
        end_index = start_index + 9;

        Realm realm = Realm.getDefaultInstance();
        RealmResults<Tour> realmResults = realm.where(Tour.class).findAll();
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

        int realm_results_last_index = realmResults.size() - 1;
        if (end_index > realm_results_last_index)
            end_index = realm_results_last_index;

        for (int i = start_index; i <= end_index; i++) {
            tours.add(realmResults.get(i));
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View list_row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_main_list, parent, false);
        list_row.setBackgroundColor(mContext.getResources().getColor(R.color.colorMaterialOrange));

        if (thumb_size_in_px == 1) {
            thumb_size_in_px = (int) MeasureUnitConversionUtils.convertDpToPixel(180.0f, mContext);
        }

        return new MyViewHolder(list_row);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Tour tour = tours.get(position);
        holder.mTitle.setText(tour.getTitle());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        holder.mDate.setText(formatter.format(tour.getDate()));
        holder.mLikes.setText("Likes: " + tour.getLikes());
        final int tour_id = tour.getId();
        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetailsActivity.mSelectedTourID = tour_id;
                Intent intent = new Intent(mContext, DetailsActivity.class);
                mContext.startActivity(intent);
            }
        });

        String[] img_refs = tour.getImg_references_str().split("---");

        Picasso.with(mContext).load(img_refs[0]).resize(thumb_size_in_px, thumb_size_in_px).centerCrop().into(holder.mImageView);

    }

    @Override
    public int getItemCount() {
        return tours.size();
    }
}

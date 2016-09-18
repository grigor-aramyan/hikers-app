package com.example.hikernotes.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hikernotes.R;
import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.activities.DetailsActivity;
import com.example.hikernotes.utils.MeasureUnitConversionUtils;

import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by John on 8/16/2016.
 */
public class MainRecyclerListAdapter extends RecyclerView.Adapter<MainRecyclerListAdapter.MyViewHolder> {
    private RealmList<Tour> mDataset;
    private Context mContext;
    private int thumb_height_in_px = 1;
    private int thumb_width_in_px = 1;

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

    public MainRecyclerListAdapter(RealmList<Tour> toursList) {
        mDataset = toursList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        CardView list_row = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_main_list, parent, false);
        list_row.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));

        if (thumb_height_in_px == 1) {
            thumb_height_in_px = (int) MeasureUnitConversionUtils.convertDpToPixel(180.0f, mContext);
        }
        if (thumb_width_in_px == 1) {
            thumb_width_in_px = parent.getWidth();
        }

        return new MyViewHolder(list_row);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Tour tour = mDataset.get(position);
        holder.mTitle.setText(tour.getTitle().toUpperCase());
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

        Glide.with(mContext).load(img_refs[0]).override(thumb_width_in_px, thumb_height_in_px).centerCrop().into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

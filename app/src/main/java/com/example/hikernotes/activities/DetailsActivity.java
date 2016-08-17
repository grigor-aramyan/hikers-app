package com.example.hikernotes.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.MainActivity;
import com.example.hikernotes.MapsActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.cache.LruBitmapCache;
import com.example.hikernotes.realms.SavedTrail;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by John on 8/16/2016.
 */
public class DetailsActivity extends AppCompatActivity {
    private TextView title_txt, date_txt, author_txt, info_txt, likes_txt;
    private ImageView map_image, image_one, image_two, image_tree, image_four, image_five;
    private Button save_trail_btn;
    private ImageView upvote_img_btn, downvote_img_btn;
    private Realm mRealm, mRealmForSavedTours;
    private RequestQueue mRequestQueue;
    private View.OnClickListener mClickListener;
    private int mSelectedTourID;
    private String mTrail, mTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mRealm = Realm.getDefaultInstance();
        mRealmForSavedTours = Realm.getDefaultInstance();
        mRequestQueue = Volley.newRequestQueue(this);

        initInterfaces();
        initViews();


        mSelectedTourID = getIntent().getIntExtra("selected-tour-id", 0);

        if (mSelectedTourID == 0) {
            Toast.makeText(this, "Sorry! Something went wrong!", Toast.LENGTH_LONG).show();
            return;
        }

        RealmResults<Tour> realmResults = mRealm.where(Tour.class).equalTo("id", mSelectedTourID).findAll();
        if (realmResults.size() > 0) {
            Tour tour = realmResults.get(0);
            title_txt.setText("Title: " + tour.getTitle());
            mTitle = tour.getTitle();
            date_txt.setText("Date: " + tour.getDate());
            likes_txt.setText("Likes: " + tour.getLikes());
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", mSelectedTourID);
        } catch (JSONException je) {}
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.sUrlForTourDetails, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String images_str = null;
                try {
                    author_txt.setText("Author: " + response.getString("author"));
                    info_txt.setText(response.getString("info"));
                    images_str = response.getString("links");
                    mTrail = response.getString("trail");
                } catch (JSONException je) {
                }
                if (images_str == null) {
                    Toast.makeText(getApplication(), "Can't fetch images! Sorry!!", Toast.LENGTH_LONG).show();
                    return;
                }
                String[] img_urls = new String[5];
                img_urls[0] = images_str.split(":::")[0];
                img_urls[1] = images_str.split(":::")[1];
                img_urls[2] = images_str.split(":::")[2];
                img_urls[3] = images_str.split(":::")[3];
                img_urls[4] = images_str.split(":::")[4];
                ImageLoader imageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(LruBitmapCache.getCacheSize(getApplication())));
                imageLoader.get(img_urls[0], ImageLoader.getImageListener(image_one, R.drawable.default_img, R.drawable.error_img));
                imageLoader.get(img_urls[1], ImageLoader.getImageListener(image_two, R.drawable.default_img, R.drawable.error_img));
                imageLoader.get(img_urls[2], ImageLoader.getImageListener(image_tree, R.drawable.default_img, R.drawable.error_img));
                imageLoader.get(img_urls[3], ImageLoader.getImageListener(image_four, R.drawable.default_img, R.drawable.error_img));
                imageLoader.get(img_urls[4], ImageLoader.getImageListener(image_five, R.drawable.default_img, R.drawable.error_img));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Error on fetching photos from db", Toast.LENGTH_LONG).show();
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void initViews() {
        title_txt = (TextView) findViewById(R.id.title_txt_id);
        date_txt = (TextView) findViewById(R.id.date_txt_id);
        author_txt = (TextView) findViewById(R.id.author_txt_id);
        info_txt = (TextView) findViewById(R.id.tour_info_txt_id);
        likes_txt = (TextView) findViewById(R.id.likes_count_txt_id);

        map_image = (ImageView) findViewById(R.id.map_img_id);
        map_image.setOnClickListener(mClickListener);
        image_one = (ImageView) findViewById(R.id.img_1_id);
        image_two = (ImageView) findViewById(R.id.img_2_id);
        image_tree = (ImageView) findViewById(R.id.img_3_id);
        image_four = (ImageView) findViewById(R.id.img_4_id);
        image_five = (ImageView) findViewById(R.id.img_5_id);

        save_trail_btn = (Button) findViewById(R.id.save_trail_btn_id);
        save_trail_btn.setOnClickListener(mClickListener);

        upvote_img_btn = (ImageView) findViewById(R.id.upvote_btn_id);
        upvote_img_btn.setOnClickListener(mClickListener);
        downvote_img_btn = (ImageView) findViewById(R.id.downvote_btn_id);
        downvote_img_btn.setOnClickListener(mClickListener);

    }

    private void initInterfaces() {
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.save_trail_btn_id:
                        mRealmForSavedTours.beginTransaction();
                        SavedTrail savedTrail = new SavedTrail(mSelectedTourID, mTitle, mTrail);

                        mRealmForSavedTours.copyToRealmOrUpdate(savedTrail);
                        mRealmForSavedTours.commitTransaction();
                        Toast.makeText(getApplication(), "Trail saved", Toast.LENGTH_LONG).show();
                        break;
                    case R.id.upvote_btn_id:
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.sUrlForVoting, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.startsWith("done")) {
                                    RealmResults<Tour> realmResults = mRealm.where(Tour.class).equalTo("id", mSelectedTourID).findAll();
                                    if (realmResults.size() > 0) {
                                        Tour tour = realmResults.get(0);
                                        int tour_likes = tour.getLikes();
                                        mRealm.beginTransaction();
                                        tour.setLikes(tour_likes + 1);
                                        mRealm.copyToRealmOrUpdate(tour);
                                        mRealm.commitTransaction();

                                        likes_txt.setText("Likes: " + (tour_likes + 1));
                                        likes_txt.setTextColor(getResources().getColor(R.color.colorMaterialGreen));

                                        upvote_img_btn.setClickable(false);
                                    }

                                    Toast.makeText(getApplication(), "Upvoted", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplication(), "Sorry! Some problem occurs", Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("flag", 1 + "");
                                params.put("id", mSelectedTourID + "");
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
                                return params;
                            }
                        };
                        mRequestQueue.add(stringRequest);
                        break;
                    case R.id.downvote_btn_id:
                        StringRequest stringRequest1 = new StringRequest(Request.Method.POST, MainActivity.sUrlForVoting, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.startsWith("done")){
                                    RealmResults<Tour> realmResults = mRealm.where(Tour.class).equalTo("id", mSelectedTourID).findAll();
                                    if (realmResults.size() > 0) {
                                        Tour tour = realmResults.get(0);
                                        int tour_likes = tour.getLikes();
                                        mRealm.beginTransaction();
                                        tour.setLikes(tour_likes - 1);
                                        mRealm.copyToRealmOrUpdate(tour);
                                        mRealm.commitTransaction();

                                        likes_txt.setText("Likes: " + (tour_likes - 1));
                                        likes_txt.setTextColor(getResources().getColor(R.color.colorMaterialRed));

                                        downvote_img_btn.setClickable(false);
                                    }

                                    Toast.makeText(getApplication(), "Downvoted", Toast.LENGTH_LONG).show();
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplication(), "Sorry! Some problem occurs", Toast.LENGTH_LONG).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("flag", 2 + "");
                                params.put("id", mSelectedTourID + "");
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("Content-Type", "application/x-www-form-urlencoded");
                                return params;
                            }
                        };
                        mRequestQueue.add(stringRequest1);
                        break;
                    case R.id.map_img_id:
                        Intent intent = new Intent(getApplication(), MapsActivity.class);
                        intent.putExtra("trail", mTrail);
                        startActivity(intent);
                    default:
                        break;
                }
            }
        };
    }
}

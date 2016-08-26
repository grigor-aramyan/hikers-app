package com.example.hikernotes.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.hikernotes.realms.SavedTrail;
import com.example.hikernotes.utils.MeasureUnitConversionUtils;
import com.example.hikernotes.widgets.AddCommentBlock;
import com.example.hikernotes.widgets.ShowCommentsBlock;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Button save_trail_btn, delete_trail_btn;
    private ImageView upvote_img_btn, downvote_img_btn;
    private Realm mRealm;
    private RequestQueue mRequestQueue;
    private View.OnClickListener mClickListener;
    public static int mSelectedTourID;
    private String mTrail, mTitle;
    private AddCommentBlock mAddCommentBlock;
    private ShowCommentsBlock mShowCommentsBlock;
    private ArrayList<ImageView> mImagesForTour = new ArrayList<>();

    private ViewPager mPreviewViewPager;
    private String[] mImg_refs;
    private MyFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (mSelectedTourID == 0) {
            Toast.makeText(this, "Sorry! Something went wrong!", Toast.LENGTH_LONG).show();
            return;
        }

        mRealm = Realm.getDefaultInstance();
        mRequestQueue = Volley.newRequestQueue(this);

        initInterfaces();
        initViews();


        RealmResults<Tour> realmResults = mRealm.where(Tour.class).equalTo("id", mSelectedTourID).findAll();
        if (realmResults.size() > 0) {
            Tour tour = realmResults.get(0);
            title_txt.setText("Title: " + tour.getTitle());
            mTitle = tour.getTitle();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
            date_txt.setText("Date: " + formatter.format(tour.getDate()));
            likes_txt.setText("Likes: " + tour.getLikes());
            author_txt.setText("Author: " + tour.getAuthor());
            info_txt.setText(tour.getInfo());
            mTrail = tour.getTrail();

            int image_size_in_px = (int) MeasureUnitConversionUtils.convertDpToPixel(150.0f, this);

            mImg_refs = tour.getImg_references_str().split("---");

            for (int i = 0; i < mImg_refs.length; i++) {
                Picasso.with(this).load(mImg_refs[i]).resize(image_size_in_px, image_size_in_px).centerCrop().into(mImagesForTour.get(i));
            }

        }

        mPreviewViewPager = (ViewPager) findViewById(R.id.img_preview_holder);
        mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mPreviewViewPager.setOffscreenPageLimit(mImg_refs.length);
        mPreviewViewPager.setAdapter(mPagerAdapter);


        RealmResults realmResults1 = mRealm.where(SavedTrail.class).equalTo("id", mSelectedTourID).findAll();
        if (realmResults1.size() > 0) {
            save_trail_btn.setVisibility(View.GONE);
            delete_trail_btn.setVisibility(View.VISIBLE);
        }

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

        // avelacnel stugum nkarneri clickneri vra

        image_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("mmm","1 image onClick");
                mPreviewViewPager.setVisibility(View.VISIBLE);
                mPreviewViewPager.setCurrentItem(0,false);
            }
        });

        image_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("mmm","2 image onClick");
                mPreviewViewPager.setVisibility(View.VISIBLE);
                mPreviewViewPager.setCurrentItem(1,false);
            }
        });



        mImagesForTour.add(image_one);
        mImagesForTour.add(image_two);
        mImagesForTour.add(image_tree);
        mImagesForTour.add(image_four);
        mImagesForTour.add(image_five);

        save_trail_btn = (Button) findViewById(R.id.save_trail_btn_id);
        save_trail_btn.setOnClickListener(mClickListener);
        delete_trail_btn = (Button) findViewById(R.id.delete_trail_btn_id);
        delete_trail_btn.setOnClickListener(mClickListener);

        upvote_img_btn = (ImageView) findViewById(R.id.upvote_btn_id);
        upvote_img_btn.setOnClickListener(mClickListener);
        downvote_img_btn = (ImageView) findViewById(R.id.downvote_btn_id);
        downvote_img_btn.setOnClickListener(mClickListener);

        mAddCommentBlock = (AddCommentBlock) findViewById(R.id.add_comment_block_id);
        mShowCommentsBlock = (ShowCommentsBlock) findViewById(R.id.show_comments_block_id);

    }

    private void initInterfaces() {
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.delete_trail_btn_id:
                        final RealmResults realmResults = mRealm.where(SavedTrail.class).equalTo("id", mSelectedTourID).findAll();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realmResults.clear();
                            }
                        });
                        Intent intent1 = new Intent(getApplicationContext(), DetailsActivity.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        break;
                    case R.id.save_trail_btn_id:
                        mRealm.beginTransaction();
                        SavedTrail savedTrail = new SavedTrail(mSelectedTourID, mTitle, mTrail);

                        mRealm.copyToRealmOrUpdate(savedTrail);
                        mRealm.commitTransaction();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }




    public static class PageFragment extends Fragment {
        static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
        static final String ARGUMENT_IMAGE_PATH = "argument_image_path";
        int pageNumber;
        private ImageView mImageHolder;
        private String mImagePath;

        static PageFragment newInstance(int page, String imagePath) {
            PageFragment pageFragment = new PageFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
            arguments.putString(ARGUMENT_IMAGE_PATH, imagePath);
            pageFragment.setArguments(arguments);
            return pageFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
            mImagePath = getArguments().getString(ARGUMENT_IMAGE_PATH);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment, null);
            mImageHolder = (ImageView) view.findViewById(R.id.preview_image_holder);
            Picasso.with(getActivity()).load(mImagePath).into(mImageHolder);
            return view;
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(position, mImg_refs[position]);
        }

        @Override
        public int getCount() {
            return mImg_refs.length;
        }

    }

    @Override
    public void onBackPressed() {
        if (mPreviewViewPager.getVisibility() == View.VISIBLE) {
            mPreviewViewPager.setVisibility(View.GONE);
            return;
        }

        super.onBackPressed();
    }


}

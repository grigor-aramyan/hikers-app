package com.example.hikernotes;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.hikernotes.activities.AddActivity;
import com.example.hikernotes.activities.EmptyActivity;
import com.example.hikernotes.adapters.MainRecyclerListAdapter;
import com.example.hikernotes.consumptions.VolleyRequests;
import com.example.hikernotes.realms.CurrentTour;
import com.example.hikernotes.realms.SavedTrail;
import com.example.hikernotes.realms.Tour;
import com.example.hikernotes.utils.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    private Realm mRealm;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private MainRecyclerListAdapter mListAdapter;
    private RealmList<Tour> mToursList;
    private int SORT_FLAG = 1;
    private LinearLayout mProgressBarLayout;
    private boolean mIsLocalDBEmpty = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        mRealm = Realm.getDefaultInstance();
        mProgressBarLayout = (LinearLayout) findViewById(R.id.progressBarLayoutId);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        SharedPreferences sharedPreferences = getSharedPreferences("sort_type", Context.MODE_PRIVATE);
        SORT_FLAG = sharedPreferences.getInt("sort_by", 1);

        RealmResults<Tour> toursInLocalDB = mRealm.where(Tour.class).findAll();
        if (toursInLocalDB.size() > 0)
            mIsLocalDBEmpty = false;

        checkNetAccessAndFetchData(this);
    }

    public void populateToursRecyclerView() {

        mToursList = new RealmList<>();


        mListAdapter = new MainRecyclerListAdapter(mToursList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadMore(page);
            }
        });

        mRecyclerView.setAdapter(mListAdapter);
        getTours(mToursList, 0, SORT_FLAG);
    }

    void loadMore(int page) {
        if (page == 0) {
            mToursList.clear();
        }

        getTours(mToursList, page, SORT_FLAG);
        mListAdapter.notifyDataSetChanged();

    }

    void getTours(RealmList<Tour> tours, int page_number, int sort_flag) {
        int start_index = page_number * 10;
        int end_index = start_index + 9;

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

        int realm_results_last_index = realmResults.size() - 1;
        if (end_index > realm_results_last_index)
            end_index = realm_results_last_index;

        for (int i = start_index; i <= end_index; i++) {
            tours.add(realmResults.get(i));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        RealmResults<SavedTrail> savedTrails = mRealm.where(SavedTrail.class).findAll();
        if (savedTrails.size() > 0) {
            SubMenu subMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 2, "Saved Trails");
            for (SavedTrail savedTrail: savedTrails) {
                subMenu.add(0, savedTrail.getId(), Menu.NONE, savedTrail.getTour_name());
            }
        }

        RealmResults<CurrentTour> currentTours = mRealm.where(CurrentTour.class).findAll();
        if (currentTours.size() > 0) {
            SubMenu subMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 3, "Current tour");
            CurrentTour currentTour = currentTours.get(0);
            subMenu.add(1, currentTour.getId(), Menu.NONE, "Current Stars");
        }

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.date_check_id:
                SharedPreferences sharedPreferences = getSharedPreferences("sort_type", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("sort_by", 1);
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("flag_continue", 2);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.likes_check_id:
                SharedPreferences sharedPreferences1 = getSharedPreferences("sort_type", MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.putInt("sort_by", 2);
                editor1.commit();
                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                intent1.putExtra("flag_continue", 2);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
                return true;
            case 10001:
                Intent intent2 = new Intent(this, AddActivity.class);
                startActivity(intent2);
                return true;
            default:
                break;
        }
        RealmResults<SavedTrail> savedTrails = mRealm.where(SavedTrail.class).findAll();
        if (savedTrails.size() > 0) {
            int item_id = item.getItemId();
            for (SavedTrail savedTrail: savedTrails) {
                if (savedTrail.getId() == item_id) {
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putExtra("trail", savedTrail.getTrail());
                    intent.putExtra("current-loc-flag", 2);
                    startActivity(intent);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences sharedPreferences = getSharedPreferences("sort_type", MODE_PRIVATE);
        int sort_flag = sharedPreferences.getInt("sort_by", 1);

        switch (sort_flag) {
            case 1:
                MenuItem menuItem = menu.findItem(R.id.date_check_id);
                menuItem.setChecked(true);
                break;
            case 2:
                MenuItem menuItem1 = menu.findItem(R.id.likes_check_id);
                menuItem1.setChecked(true);
                break;
            default:
                break;
        }

        return true;
    }

    private void checkNetAccessAndFetchData(Context context) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForConnectivityCheck, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.startsWith("got it")) {
                    mRecyclerView.setVisibility(View.GONE);
                    mSearchView.setVisibility(View.GONE);
                    mProgressBarLayout.setVisibility(View.VISIBLE);
                    updateLocalDBAndPopulateList();
                } else if (!mIsLocalDBEmpty) {
                    Toast.makeText(getApplicationContext(), "Data is from local DB", Toast.LENGTH_LONG).show();
                    mProgressBarLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mSearchView.setVisibility(View.VISIBLE);
                    populateToursRecyclerView();
                } else {
                    Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!mIsLocalDBEmpty) {
                    Toast.makeText(getApplicationContext(), "Data is from local DB", Toast.LENGTH_LONG).show();
                    mProgressBarLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mSearchView.setVisibility(View.VISIBLE);
                    populateToursRecyclerView();
                } else {
                    Intent intent = new Intent(getApplicationContext(), EmptyActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        VolleyRequests.getQueue(context).add(stringRequest);

    }


    private void updateLocalDBAndPopulateList() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(VolleyRequests.sUrlForDataUpdate, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 1) {
                    JSONObject jsonObject;
                    Tour tour = null;
                    String title, date, references, author, info, trail, onMapImages;
                    int id, likes;
                    for (int i = 0; i < (response.length() - 1); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            title = jsonObject.getString("title");
                            date = jsonObject.getString("date");
                            references = jsonObject.getString("references");
                            author = jsonObject.getString("author");
                            info = jsonObject.getString("info");
                            id = jsonObject.getInt("id");
                            likes = jsonObject.getInt("likes");
                            trail = jsonObject.getString("trail");
                            onMapImages = jsonObject.getString("onmapimages");

                            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

                            mRealm.beginTransaction();
                            try {
                                tour = new Tour(id, author, title, formatter.parse(date), info, likes, references, trail, onMapImages);
                            } catch (ParseException pExp) {
                                if (null == tour) {
                                    Toast.makeText(getApplicationContext(), "Major problem occured!! Sorry, guys. Doing best to make it better!!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            mRealm.copyToRealmOrUpdate(tour);
                            mRealm.commitTransaction();
                        } catch (JSONException jsconExp) { Log.e("yyy", "some json exp");}
                    }

                    mProgressBarLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mSearchView.setVisibility(View.VISIBLE);
                    populateToursRecyclerView();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!mIsLocalDBEmpty) {
                    Toast.makeText(getApplicationContext(), "Data is from local DB", Toast.LENGTH_LONG).show();
                    populateToursRecyclerView();
                }

            }
        });
        VolleyRequests.getQueue(getApplicationContext()).add(jsonArrayRequest);
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

}

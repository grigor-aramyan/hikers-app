package com.example.hikernotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hikernotes.activities.AddActivity;
import com.example.hikernotes.adapters.MyPagerAdapter;
import com.example.hikernotes.realms.CurrentTour;
import com.example.hikernotes.realms.SavedTrail;
import com.example.hikernotes.realms.Tour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    public static String sUrlForConnectivityCheck = "http://hikingapp.net23.net/checknetaccess.php";
    public static String sUrlForDataUpdate = "http://hikingapp.net23.net/updateappdata.php";
    public static String sUrlForVoting = "http://hikingapp.net23.net/vote.php";
    public static String sUrlForTourDetails = "http://hikingapp.net23.net/getremainingdata.php";
    public static String sUrlForNewTourAdd = "http://hikingapp.net23.net/addnewtour.php";
    public static String sUrlForImageUploads = "http://hikingapp.net23.net/storetourimages.php";
    public static String sUrlForNewComment = "http://hikingapp.net23.net/addnewcomment.php";
    public static String sUrlForPullingComments = "http://hikingapp.net23.net/pullcomments.php";

    private ViewPager mViewPager;
    private RequestQueue mQueue;
    private Realm mRealm;
    private MyPagerAdapter adapter;

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

        mQueue = Volley.newRequestQueue(this);
        mRealm = Realm.getDefaultInstance();

        int flag = getIntent().getIntExtra("flag_continue", 1);

        if (flag != 2) {
            checkConnectivity();
            return;
        }

        mViewPager = (ViewPager) findViewById(R.id.pager_main);
        mViewPager.setPageTransformer(false, new CubeOutTransformer());

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
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

        // Inflate the menu; this adds items to the action bar if it is present.
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
                //Toast.makeText(this, "likes sort", Toast.LENGTH_LONG).show();
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

    private void checkConnectivity() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, sUrlForConnectivityCheck, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // if true, net connection is available, we can fetch updated data from db
                if (response.startsWith("got it")) {
                    updateLocalDB();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "No connection! Data is from local DB!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("flag_continue", 2);
                startActivity(intent);
            }
        });
        mQueue.add(stringRequest);
    }

    private void updateLocalDB() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(sUrlForDataUpdate, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 1) {
                    JSONObject jsonObject;
                    Tour tour;
                    String title, date, reference;
                    int id, likes;
                    for (int i = 0; i < (response.length() - 1); i++) {
                        try {
                            jsonObject = response.getJSONObject(i);
                            title = jsonObject.getString("title");
                            date = jsonObject.getString("date");
                            reference = jsonObject.getString("reference");
                            id = jsonObject.getInt("id");
                            likes = jsonObject.getInt("likes");

                            mRealm.beginTransaction();
                            tour = new Tour(id, title, date, likes, reference);
                            mRealm.copyToRealmOrUpdate(tour);
                            mRealm.commitTransaction();
                        } catch (JSONException jsconExp) { Log.e("yyy", "some json exp");}
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("flag_continue", 2);
                    startActivity(intent);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplication(), "Couldn't retrieve data from serv!! Locals will be used!!", Toast.LENGTH_LONG).show();
            }
        });
        mQueue.add(jsonArrayRequest);
    }

    @Override
    public void onBackPressed() {
        if (mViewPager == null || mViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }
}

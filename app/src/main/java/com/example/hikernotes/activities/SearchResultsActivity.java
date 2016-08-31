package com.example.hikernotes.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.hikernotes.R;
import com.example.hikernotes.adapters.MainRecyclerListAdapter;
import com.example.hikernotes.realms.Tour;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by John on 8/30/2016.
 */
public class SearchResultsActivity extends Activity {
    private RealmList<Tour> mRealmList;
    private TextView mTextViewNoResults;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        initViews();

        checkDBForMatch(getIntent());

        showResults();
    }

    private void initViews() {
        mTextViewNoResults = (TextView) findViewById(R.id.no_results_id);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_list);
    }

    private void showResults() {
        if (null == mRealmList) {
            mRecyclerView.setVisibility(View.GONE);
            mTextViewNoResults.setVisibility(View.VISIBLE);
        } else {
            MainRecyclerListAdapter adapter = new MainRecyclerListAdapter(mRealmList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        checkDBForMatch(intent);

        showResults();
    }

    private void checkDBForMatch(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Realm realm = Realm.getDefaultInstance();
            RealmResults<Tour> realmResults = realm.where(Tour.class).contains("title", query).findAll();

            if (realmResults.size() > 0) {
                mRealmList = new RealmList<>();
                for (Tour tour: realmResults) {
                    mRealmList.add(tour);
                }
            }
        }
    }
}

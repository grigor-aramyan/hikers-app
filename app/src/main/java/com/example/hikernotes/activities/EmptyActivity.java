package com.example.hikernotes.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.hikernotes.R;

/**
 * Created by John on 8/27/2016.
 */
public class EmptyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);

        Toast.makeText(this, "No net connection, pal!! Can't retrieve data from servs!!", Toast.LENGTH_LONG).show();
    }
}

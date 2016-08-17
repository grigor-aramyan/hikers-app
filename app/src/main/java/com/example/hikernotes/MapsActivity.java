package com.example.hikernotes;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.hikernotes.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String mTrail;
    private boolean mCurrentLocationEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTrail = getIntent().getStringExtra("trail");
        int cur_loc_flag = getIntent().getIntExtra("current-loc-flag", 1);
        if (cur_loc_flag == 2)
            mCurrentLocationEnabled = true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        PolylineOptions trail = new PolylineOptions();
        trail.color(R.color.colorTrail);
        String[] coordinates = mTrail.split("::");
        LatLng firstltlng = null;
        LatLng latLng = null;
        String[] latLng_str;
        for (int i = 0; i < (coordinates.length - 1); i++) {
            if (coordinates[i].isEmpty())
                break;
            latLng_str = coordinates[i].split("-");
            latLng = new LatLng(Double.parseDouble(latLng_str[0]), Double.parseDouble(latLng_str[1]));
            trail.add(latLng);
            if (i == 0)
                firstltlng = latLng;
        }

        mMap.addMarker(new MarkerOptions()
        .position(firstltlng)
        .title("Start Point"));
        mMap.addPolyline(trail);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        if (mCurrentLocationEnabled) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException sExp) {}
        }

    }
}

package com.example.hikernotes.services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.hikernotes.utils.NotificationUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by John on 8/17/2016.
 */
public class LocationUpdateService extends Service {
    public static final int REQUEST_CODE_FOR_RESOLUTION_REQUEST = 1;
    public static Activity sActivity;
    public static String sSharedPrefForFixedLocations = "locationsBase";
    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Toast.makeText(getApplication(), "Sorry! No connection to update location!!", Toast.LENGTH_LONG).show();
        }
    };
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Location location = null;
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } catch (SecurityException se){
                Toast.makeText(getApplication(), "security exception", Toast.LENGTH_LONG).show();
            }

            if (location != null) {
                saveCoordinatesInSharedPreference(location);
                NotificationUtils.showUpdatedLocationNotification(getApplication(), location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(getApplication(), "Null location!!", Toast.LENGTH_LONG).show();
            }

            checkLocationSettings();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };
    private ResultCallback<LocationSettingsResult> mLocationSettingsResultCallback = new ResultCallback<LocationSettingsResult>() {
        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
            Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdates();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(sActivity, REQUEST_CODE_FOR_RESOLUTION_REQUEST);
                    } catch (IntentSender.SendIntentException se) {}
                    break;
            }
        }
    };
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            saveCoordinatesInSharedPreference(location);
            NotificationUtils.showUpdatedLocationNotification(getApplication(), location.getLatitude(), location.getLongitude());
        }
    };
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    // ToDo: make this fields available for user input
    private long mLocationUpdateInterval = 10000L;
    private long mLocationUpdateIntervalFastest = 5000L;

    @Override
    public void onCreate() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mLocationUpdateInterval);
        mLocationRequest.setFastestInterval(mLocationUpdateIntervalFastest);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    public void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(mLocationSettingsResultCallback);
    }

    private void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
        } catch (SecurityException se3) {}
    }

    private void saveCoordinatesInSharedPreference(Location location) {
        if (location == null)
            return;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String point = latitude + "-" + longitude + "::";

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(sSharedPrefForFixedLocations, Context.MODE_PRIVATE);
        String pastLocations = sharedPreferences.getString("locations", "");
        pastLocations = pastLocations + point;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("locations", pastLocations);
        editor.commit();
    }
}

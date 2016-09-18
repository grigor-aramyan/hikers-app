package com.example.hikernotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hikernotes.activities.DetailsActivity;
import com.example.hikernotes.services.LocationUpdateService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int MAKE_A_SHOT_REQUEST_CODE = 22;
    public static final String ON_MAP_IMAGES_KEY = "saved-on-map-images";
    public static final String ON_MAP_IMAGES_PREFERENCE = "saved-on-map-images-preference";
    private GoogleMap mMap;
    private String mTrail;
    private boolean mCurrentLocationEnabled = false, fromDetails = false;
    private ArrayList<String> mOnMapImageReferences;
    private ArrayList<String> mOnMapImageCoordinates;
    private ArrayList<String> mOnMapImageReferencesCurrent, mOnMapImageCoordinatesCurrent;
    private ImageView mTakeAShot;
    private String mCurrentPhotoPath;
    private ViewPager mPreviewViewPager;
    private MyFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mOnMapImageReferences = new ArrayList<>();
        mOnMapImageCoordinates = new ArrayList<>();
        mOnMapImageCoordinatesCurrent = new ArrayList<>();
        mOnMapImageReferencesCurrent = new ArrayList<>();

        mPreviewViewPager = (ViewPager) findViewById(R.id.img_preview_holder);
        mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mPreviewViewPager.setOffscreenPageLimit(2);
        mPreviewViewPager.setAdapter(mPagerAdapter);

        mTakeAShot = (ImageView) findViewById(R.id.make_shot_id);
        mTakeAShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMapImageReferencesCurrent.size() != 5) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getApplicationContext(), "Only 5 images allowed!! Sorry, pal!!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // if value is 2, means maps activity was started from details activity
        int fromDetailsActivity = getIntent().getIntExtra("from-details", 1);
        if (fromDetailsActivity == 2) {
            mTakeAShot.setVisibility(View.GONE);
            fromDetails = true;

            String onMapImagesEncoded = getIntent().getStringExtra("on-map-images");
            if (!onMapImagesEncoded.isEmpty()) {
                // encoding scheme - "ref::lat-longUUUU"
                String[] tempArray = onMapImagesEncoded.split("UUUU");
                String[] imageInfo;
                for (String s: tempArray) {
                    imageInfo = s.split("::");
                    mOnMapImageReferences.add(imageInfo[0]);
                    mOnMapImageCoordinates.add(imageInfo[1]);
                }

                mPagerAdapter.notifyDataSetChanged();
            }
        }

        mTrail = getIntent().getStringExtra("trail");
        int cur_loc_flag = getIntent().getIntExtra("current-loc-flag", 1);
        if (cur_loc_flag == 2)
            mCurrentLocationEnabled = true;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, MAKE_A_SHOT_REQUEST_CODE);
            }
        }
    }

    private boolean isRealFile() {
        return new File(mCurrentPhotoPath).isFile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MAKE_A_SHOT_REQUEST_CODE:
                    if (isRealFile()) {
                        mOnMapImageReferencesCurrent.add(mCurrentPhotoPath);
                        mOnMapImageCoordinatesCurrent.add(getLastFixedLocation());
                        galleryAddPic();
                        drawMarkers(1);
                    }
                    break;

                default:
                    break;
            }
        }
    }


    private void drawMarkers(int flagForInitialActivity) {
        // flag; 1 - from AddActivity, 2 - from DetailsActivity
        if (flagForInitialActivity == 1) {
            for (int i = 0; i < mOnMapImageCoordinatesCurrent.size(); i++) {
                String latLongEncoded = mOnMapImageCoordinatesCurrent.get(i);
                String[] latLong = latLongEncoded.split("-");
                LatLng latLng = new LatLng(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_photo_white_36dp)));
            }
        } else if (flagForInitialActivity == 2) {
            for (int i = 0; i < mOnMapImageCoordinates.size(); i++) {
                String latLongEncoded = mOnMapImageCoordinates.get(i);
                String[] latLong = latLongEncoded.split("-");
                LatLng latLng = new LatLng(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]));

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_photo_white_36dp)));
                marker.setTag(i);
            }
        }
    }

    private String getLastFixedLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
        String fixedCoordinatesEncoded = sharedPreferences.getString("locations", "");
        // encode scheme "lat-long::"
        String[] fixedLocations = fixedCoordinatesEncoded.split("::");
        String lastFixedLocation = fixedLocations[(fixedLocations.length - 1)];
        return lastFixedLocation;
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        if (mCurrentLocationEnabled) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException sExp) {}
        }

        if (!fromDetails) {
            SharedPreferences sharedPreferences = getSharedPreferences(ON_MAP_IMAGES_PREFERENCE, MODE_PRIVATE);
            String currentOnMapImagesEncoded = sharedPreferences.getString(ON_MAP_IMAGES_KEY, "");

            if (!currentOnMapImagesEncoded.isEmpty()) {
                // encode scheme "ref::lat-longYYY"
                String[] imagesInfo = currentOnMapImagesEncoded.split("YYY");
                String[] singleImageInfo;
                for (String s: imagesInfo) {
                    singleImageInfo = s.split("::");
                    mOnMapImageReferencesCurrent.add(singleImageInfo[0]);
                    mOnMapImageCoordinatesCurrent.add(singleImageInfo[1]);
                }
                drawMarkers(1);
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if ((null != marker.getTag()) && fromDetails) {
                    mPreviewViewPager.setVisibility(View.VISIBLE);
                    mPreviewViewPager.setCurrentItem((Integer) marker.getTag(), false);
                    return true;
                }

                return false;
            }
        });

        if (fromDetails && mOnMapImageReferences.size() != 0) {
            drawMarkers(2);
        }

    }

    @Override
    protected void onDestroy() {
        if (!fromDetails) {
            String currentOnMapImagesEncoded = "";
            for (int i = 0; i < mOnMapImageCoordinatesCurrent.size(); i++) {
                currentOnMapImagesEncoded = currentOnMapImagesEncoded + mOnMapImageReferencesCurrent.get(i) + "::" + mOnMapImageCoordinatesCurrent.get(i) + "YYY";
            }

            if (!currentOnMapImagesEncoded.isEmpty()) {
                SharedPreferences sharedPreferences = getSharedPreferences(ON_MAP_IMAGES_PREFERENCE, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(ON_MAP_IMAGES_KEY, currentOnMapImagesEncoded);
                editor.commit();
            }

        }

        super.onDestroy();

    }

    public static class PageFragment2 extends Fragment {
        static final String ARGUMENT_PAGE_NUMBER_2 = "arg_page_number";
        static final String ARGUMENT_IMAGE_PATH_2 = "argument_image_path";
        int pageNumber;
        private ImageView mImageHolder;
        private String mImagePath;

        public static PageFragment2 newInstance(int page, String imagePath) {
            PageFragment2 pageFragment = new PageFragment2();
            Bundle arguments = new Bundle();
            arguments.putInt(ARGUMENT_PAGE_NUMBER_2, page);
            arguments.putString(ARGUMENT_IMAGE_PATH_2, imagePath);
            pageFragment.setArguments(arguments);
            return pageFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER_2);
            mImagePath = getArguments().getString(ARGUMENT_IMAGE_PATH_2);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment, null);
            mImageHolder = (ImageView) view.findViewById(R.id.preview_image_holder);
            WindowManager wm = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            int imageSize;
            imageSize = Math.min(metrics.heightPixels, metrics.widthPixels);

            Glide.with(getContext()).load(mImagePath)
                    .placeholder(R.drawable.loader)
                    .error(R.drawable.noimageavailable)
                    .override(imageSize, imageSize).centerCrop().dontAnimate()
                    .into(mImageHolder);

            return view;
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment2.newInstance(position, mOnMapImageReferences.get(position));
        }

        @Override
        public int getCount() {
            return mOnMapImageReferences.size();
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

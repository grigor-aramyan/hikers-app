package com.example.hikernotes.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.hikernotes.MapsActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.consumptions.VolleyRequests;
import com.example.hikernotes.realms.CurrentTour;
import com.example.hikernotes.services.LocationUpdateService;
import com.example.hikernotes.utils.MeasureUnitConversionUtils;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;


public class AddActivity extends AppCompatActivity {

    private final int IMAGES_MAX_QNTY = 5;
    private final int IDD_LIST_CATS = 1;
    private static final int SELECT_PICTURE = 100;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.save_tour_btn_id:
                    String title = title_edt.getText().toString();
                    String author = author_edt.getText().toString();
                    String info = info_edt.getText().toString();

                    String image_uris_encoded = "";
                    if (mImage_uris.size() != 0) {
                        for (Uri uri: mImage_uris) {
                            image_uris_encoded += uri.toString() + "----";
                        }
                    }
                    RealmResults<CurrentTour> realmResults = mRealm.where(CurrentTour.class).findAll();
                    if (realmResults.size() == 0) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                        mRealm.beginTransaction();
                        CurrentTour currentTour = new CurrentTour(title, author, formatter.format(new Date()), info, image_uris_encoded);
                        mRealm.copyToRealmOrUpdate(currentTour);
                        mRealm.commitTransaction();
                        Toast.makeText(getApplication(), "Current tour saved!", Toast.LENGTH_LONG).show();
                    } else {
                        CurrentTour currentTour = realmResults.get(0);
                        mRealm.beginTransaction();
                        currentTour.setTitle(title);
                        currentTour.setAuthor(author);
                        currentTour.setInfo(info);
                        currentTour.setTour_imgs_refs(image_uris_encoded);
                        mRealm.copyToRealmOrUpdate(currentTour);
                        mRealm.commitTransaction();
                        Toast.makeText(getApplication(), "Current tour updated!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.clear_current_btn_id:
                    final RealmResults<CurrentTour> realmResults1 = mRealm.where(CurrentTour.class).findAll();
                    if (realmResults1.size() == 0) {
                        Toast.makeText(getApplication(), "No current tour! Nothing to clear!!", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                        AlertDialog alertDialog = builder
                                .setTitle("Clearing current")
                                .setMessage("You are about clearing inputed data and previously fixed locations??")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        clearLocationsSharedPref();

                                        mRealm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realmResults1.clear();
                                            }
                                        });

                                        Toast.makeText(getApplication(), "Cleared!", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplication(), AddActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                    break;

                case R.id.upload_tour_btn_id:
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, VolleyRequests.sUrlForConnectivityCheck, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.startsWith("got it")) {
                                final RealmResults<CurrentTour> realmResults2 = mRealm.where(CurrentTour.class).findAll();
                                if (realmResults2.size() == 0) {
                                    Toast.makeText(getApplication(), "Nothing to upload!!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                String author, title, info, date, trail;
                                author = author_edt.getText().toString();
                                title = title_edt.getText().toString();
                                info = info_edt.getText().toString();
                                if (author.isEmpty() || title.isEmpty() || info.isEmpty()) {
                                    Toast.makeText(getApplication(), "No empty fields allowed! Input some data before upload!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                CurrentTour currentTour = realmResults2.get(0);
                                date = currentTour.getDate();
                                SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
                                trail = sharedPreferences.getString("locations", "");
                                if (trail.isEmpty()) {
                                    Toast.makeText(getApplication(), "No fixed locations, those trail for this tour. Can't upload w/o it!!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (size != 5) {
                                    Toast.makeText(getApplication(), "We need exactly 5 images to make an upload. Add some, please!", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("author", author);
                                    jsonObject.put("title", title);
                                    jsonObject.put("info", info);
                                    jsonObject.put("date", date);
                                    jsonObject.put("trail", trail);
                                } catch (JSONException jExp) {}
                                Toast.makeText(getApplicationContext(), "We're uploading the data", Toast.LENGTH_LONG).show();
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, VolleyRequests.sUrlForNewTourAdd, jsonObject, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            if (response.getString("result").equals("ok")) {
                                                int new_tour_id = response.getInt("lastid");

                                                for (int i = 0; i < mImageUris.size(); i++) {
                                                    if (null == mImageUris.get(i))
                                                        continue;
                                                    String imagePath = getPathFromURI(mImageUris.get(i));

                                                    MultipartUploadRequest request = new MultipartUploadRequest(getApplication(), VolleyRequests.sUrlForImageUploads)
                                                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                                                            .setMaxRetries(3)
                                                            .addParameter("tourid", new_tour_id + "")
                                                            .addFileToUpload(imagePath, "myimage");
                                                    request.setDelegate(mUploadStatusDelegate).startUpload();
                                                }

                                                SharedPreferences sharedPreferences1 = getSharedPreferences(MapsActivity.ON_MAP_IMAGES_PREFERENCE, MODE_PRIVATE);
                                                String onMapImagesEncoded = sharedPreferences1.getString(MapsActivity.ON_MAP_IMAGES_KEY, "");
                                                if (!onMapImagesEncoded.isEmpty()) {
                                                    String[] onMapImages = onMapImagesEncoded.split("YYY");
                                                    ArrayList<String> imageReferences = new ArrayList<>();
                                                    ArrayList<String> imageCoordinates = new ArrayList<>();
                                                    String[] imageInfo;
                                                    for (String s: onMapImages) {
                                                        imageInfo = s.split("::");
                                                        imageReferences.add(imageInfo[0]);
                                                        imageCoordinates.add(imageInfo[1]);
                                                    }

                                                    for (int j = 0; j < imageReferences.size(); j++) {
                                                        MultipartUploadRequest request = new MultipartUploadRequest(getApplication(), VolleyRequests.sUrlForOnMapImagesUpload)
                                                                .setAutoDeleteFilesAfterSuccessfulUpload(false)
                                                                .setMaxRetries(3)
                                                                .addParameter("tourid", new_tour_id + "")
                                                                .addParameter("imagecoord", imageCoordinates.get(j))
                                                                .addFileToUpload(imageReferences.get(j), "myimage");
                                                        request.setDelegate(mUploadStatusDelegate).startUpload();
                                                    }

                                                    clearOnMapImagesSharedPref();
                                                }

                                            }
                                        } catch (JSONException jsExp) {
                                            Toast.makeText(getApplication(), "jsonexception while parsing jsonobject response", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        catch (FileNotFoundException fExp) {
                                            Toast.makeText(getApplication(), "Can't find some images to upload! Try to pick other one!!", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        catch (MalformedURLException malExp) {
                                            Toast.makeText(getApplication(), "Some problem with our serv! Try to upload later! Sorry!", Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        clearLocationsSharedPref();

                                        mRealm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realmResults2.clear();
                                            }
                                        });
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplication(), "some problem while making jsonobject req", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                });
                                mRequestQueue.add(jsonObjectRequest);

                            } else {
                                Toast.makeText(getApplication(), "Sorry! We have problems with our server! Try later, please!!", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplication(), "Sorry! Some problems with net connection!", Toast.LENGTH_LONG).show();

                        }
                    });
                    mRequestQueue.add(stringRequest);
                    break;

                case R.id.imgage_plus:


                    switch (i) {
                        case 0:
                            mImg = tour_img_one;
                            openImageChooser();

                            break;

                        case 1:
                            mImg = tour_img_two;
                            openImageChooser();

                            break;

                        case 2:
                            mImg = tour_img_tree;
                            openImageChooser();

                            break;

                        case 3:
                            mImg = tour_img_four;
                            openImageChooser();

                            break;

                        case 4:

                            mImg = tour_img_five;
                            openImageChooser();



                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    };
    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId()) {

                case R.id.img_1_id:

                    i = 0;
                    mImg = tour_img_one;
                    showDialog(IDD_LIST_CATS);
                    break;

                case R.id.img_2_id:

                    i = 1;
                    mImg = tour_img_two;
                    showDialog(IDD_LIST_CATS);
                    break;

                case R.id.img_3_id:

                    i = 2;
                    mImg = tour_img_tree;
                    showDialog(IDD_LIST_CATS);
                    break;

                case R.id.img_4_id:

                    i = 3;
                    mImg = tour_img_four;
                    showDialog(IDD_LIST_CATS);
                    break;

                case R.id.img_5_id:

                    i = 4;
                    mImg = tour_img_five;
                    showDialog(IDD_LIST_CATS);
                    break;

                default:
                    return false;
            }
            return true;
        }
    };
    private UploadStatusDelegate mUploadStatusDelegate = new UploadStatusDelegate() {
        @Override
        public void onProgress(UploadInfo uploadInfo) {
        }

        @Override
        public void onError(UploadInfo uploadInfo, Exception exception) {
            Toast.makeText(getApplication(), "Some error while trying to upload images!", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCompleted(UploadInfo uploadInfo, ServerResponse serverResponse) {
            Toast.makeText(getApplication(), "Images upload completed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCancelled(UploadInfo uploadInfo) {
        }
    };
    private EditText author_edt, title_edt, info_edt;
    private ImageView tour_img_one, tour_img_two, tour_img_tree, tour_img_four, tour_img_five, image_plus, mImg;
    private Button save_btn, upload_btn, clear_btn;
    private ArrayList<Uri> mImage_uris = new ArrayList<>();
    private ArrayList<ImageView> tour_images = new ArrayList<>();
    private Realm mRealm;
    private RequestQueue mRequestQueue;
    private int mImageSizeInPx;
    private int i = 0;
    private ArrayList<Uri> mImageUris;
    private Intent mIntentOfLocationUpdateService;
    private int size = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mImageUris = new ArrayList<>();
        for(int j= 0;j<5;j++){
            mImageUris.add(j,null);
        }


        mRealm = Realm.getDefaultInstance();
        mRequestQueue = Volley.newRequestQueue(this);

        mImageSizeInPx = (int) MeasureUnitConversionUtils.convertDpToPixel(160.0f, this);

        initViews();

        RealmResults<CurrentTour> realmResults = mRealm.where(CurrentTour.class).findAll();
        if (realmResults.size() != 0) {
            CurrentTour currentTour = realmResults.get(0);
            if (null != currentTour.getTitle())
                title_edt.setText(currentTour.getTitle());
            if (null != currentTour.getAuthor())
                author_edt.setText(currentTour.getAuthor());
            if (null != currentTour.getInfo())
                info_edt.setText(currentTour.getInfo());
            if (null != currentTour.getTour_imgs_refs() && !currentTour.getTour_imgs_refs().isEmpty()) {
                String[] img_uris = currentTour.getTour_imgs_refs().split("----");
                for (int i = 0; i < img_uris.length; i++) {
                    if (!img_uris[i].isEmpty())
                        mImage_uris.add(Uri.parse(img_uris[i]));
                }
                setImageViewsSrc();
            }

        }

        mIntentOfLocationUpdateService = new Intent(this, LocationUpdateService.class);

    }

    // reserve method
    private void setImageViewsSrc() {

        int uri_qnt = mImage_uris.size();
        if (uri_qnt == 0)
            return;
        for (int i = 0; i < uri_qnt; i++) {
            Glide.with(this).load(new File(mImage_uris.get(i).toString())).override(mImageSizeInPx, mImageSizeInPx).centerCrop().into(tour_images.get(i));
        }

        int remaining_view_count = IMAGES_MAX_QNTY - uri_qnt;
        if (remaining_view_count == 0)
            return;

        Glide.with(this).load(R.drawable.add_button).override(mImageSizeInPx, mImageSizeInPx).centerCrop().into(tour_images.get(uri_qnt));

        tour_images.get(uri_qnt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImages();
            }
        });
    }

    // reserve method
    private void getImages() {
        int selectionLimit = IMAGES_MAX_QNTY - mImage_uris.size();
        if (selectionLimit == 0) {
            Toast.makeText(this, "Only 5 images allowed!! Delete some first by long clicking on them!!", Toast.LENGTH_LONG).show();
            return;
        }

    }

    private void initViews() {

        author_edt = (EditText) findViewById(R.id.author_txt_id);
        title_edt = (EditText) findViewById(R.id.title_txt_id);
        info_edt = (EditText) findViewById(R.id.tour_info_txt_id);

        image_plus = (ImageView) findViewById(R.id.imgage_plus) ;
        image_plus.setOnClickListener(mClickListener);


        tour_img_one = (ImageView) findViewById(R.id.img_1_id);
        tour_img_one.setOnLongClickListener(mLongClickListener);
        tour_img_one.setVisibility(View.GONE);
        tour_images.add(tour_img_one);

        tour_img_two = (ImageView) findViewById(R.id.img_2_id);
        tour_img_two.setOnLongClickListener(mLongClickListener);
        tour_img_two.setVisibility(View.GONE);
        tour_images.add(tour_img_two);

        tour_img_tree = (ImageView) findViewById(R.id.img_3_id);
        tour_img_tree.setOnLongClickListener(mLongClickListener);
        tour_img_tree.setVisibility(View.GONE);
        tour_images.add(tour_img_tree);

        tour_img_four = (ImageView) findViewById(R.id.img_4_id);
        tour_img_four.setOnLongClickListener(mLongClickListener);
        tour_img_four.setVisibility(View.GONE);
        tour_images.add(tour_img_four);

        tour_img_five = (ImageView) findViewById(R.id.img_5_id);
        tour_img_five.setOnLongClickListener(mLongClickListener);
        tour_img_five.setVisibility(View.GONE);
        tour_images.add(tour_img_five);

        save_btn = (Button) findViewById(R.id.save_tour_btn_id);
        save_btn.setOnClickListener(mClickListener);
        upload_btn = (Button) findViewById(R.id.upload_tour_btn_id);
        upload_btn.setOnClickListener(mClickListener);
        clear_btn = (Button) findViewById(R.id.clear_current_btn_id);
        clear_btn.setOnClickListener(mClickListener);
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }


    private void clearLocationsSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("locations");
        editor.commit();
    }

    private void clearOnMapImagesSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(MapsActivity.ON_MAP_IMAGES_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(MapsActivity.ON_MAP_IMAGES_KEY);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_activity_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.map_img_id:
                SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
                String current_trail = sharedPreferences.getString("locations", "");
                if (current_trail.isEmpty()) {
                    Toast.makeText(getApplication(), "No current trail to show!!", Toast.LENGTH_LONG).show();
                    return true;
                }
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("trail", current_trail);
                startActivity(intent);
                return true;

            case R.id.start_tracking_id:
                stopService(mIntentOfLocationUpdateService);
                LocationUpdateService.sActivity = AddActivity.this;
                startService(mIntentOfLocationUpdateService);

                RealmResults<CurrentTour> realmResults = mRealm.where(CurrentTour.class).findAll();
                if (realmResults.size() == 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                    mRealm.beginTransaction();
                    CurrentTour currentTour = new CurrentTour();
                    currentTour.setDate(formatter.format(new Date()));
                    mRealm.copyToRealmOrUpdate(currentTour);
                    mRealm.commitTransaction();
                    Toast.makeText(getApplication(), "Current tour saved!", Toast.LENGTH_LONG).show();
                }

                return true;

            case R.id.stop_tracking_id:
                stopService(mIntentOfLocationUpdateService);
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {

                    if (!selectedImageUri.toString().isEmpty()) {
                        Glide.with(this).load(selectedImageUri).override(200, 200).centerCrop().into(mImg);
                        mImg.setVisibility(View.VISIBLE);
                        mImageUris.set(i, selectedImageUri);
                        size ++;
                        i++;
                    }

                }
                    if (size == 5) {
                        image_plus.setVisibility(View.GONE);}
                    else{
                        image_plus.setVisibility(View.VISIBLE);
                    }
            }

            if (requestCode == LocationUpdateService.REQUEST_CODE_FOR_RESOLUTION_REQUEST) {
                Toast.makeText(this, "If you enabled settings, try to start tracking again!!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_LIST_CATS:

                final String[] mTables ={"INSERT", "DELETE", "BACK"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose something");

                builder.setItems(mTables, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:
                                size--;
                                openImageChooser();
                                break;

                            case 1:
                                size--;
                                mImg.setVisibility(View.GONE);
                                image_plus.setVisibility(View.VISIBLE);
                                mImageUris.set(i,null);

                                break;

                            case 2:

                                break;

                        }
                    }
                });

                builder.setCancelable(false);
                return builder.create();

            default:
                return null;
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
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

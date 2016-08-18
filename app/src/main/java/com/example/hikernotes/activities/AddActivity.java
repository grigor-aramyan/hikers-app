package com.example.hikernotes.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.hikernotes.MapsActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.realms.CurrentTour;
import com.example.hikernotes.services.LocationUpdateService;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by John on 8/16/2016.
 */
public class AddActivity extends AppCompatActivity {
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private final int IMAGES_MAX_QNTY = 5;
    private Intent mIntentOfLocationUpdateService;
    private View.OnClickListener mClickListener;
    private View.OnLongClickListener mLongClickListener;
    private EditText author_edt, title_edt, info_edt;
    private ImageView map_img, tour_img_one, tour_img_two, tour_img_tree, tour_img_four, tour_img_five;
    private Button save_btn, upload_btn, clear_btn;
    private ArrayList<Uri> mImage_uris = new ArrayList<>();
    private ArrayList<ImageView> tour_images = new ArrayList<>();
    private Realm mRealm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        mRealm = Realm.getDefaultInstance();

        initInterfaces();
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
                    mImage_uris.add(Uri.parse(img_uris[i]));
                }
                setImageViewsSrc();
            }

        }

        mIntentOfLocationUpdateService = new Intent(this, LocationUpdateService.class);
    }

    private void setImageViewsSrc() {
        int uri_qnt = mImage_uris.size();
        if (uri_qnt == 0)
            return;
        Bitmap bitmap = null;
        for (int i = 0; i < uri_qnt; i++) {
            bitmap = BitmapFactory.decodeFile(mImage_uris.get(i).toString());
            tour_images.get(i).setImageBitmap(bitmap);
        }

        int remaining_view_count = IMAGES_MAX_QNTY - uri_qnt;
        if (remaining_view_count == 0)
            return;

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img);
        for (int j = 4; j > (IMAGES_MAX_QNTY - remaining_view_count - 1); j--) {
            tour_images.get(j).setImageBitmap(bitmap);
        }
    }

    private void getImages() {
        int selectionLimit = IMAGES_MAX_QNTY - mImage_uris.size();
        if (selectionLimit == 0) {
            Toast.makeText(this, "Only 5 images allowed!! Delete some first by long clicking on them!!", Toast.LENGTH_LONG).show();
            return;
        }
        Config config = new Config();
        config.setSelectionLimit(selectionLimit);

        ImagePickerActivity.setConfig(config);

        Intent intent = new Intent(this, ImagePickerActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
    }

    private void initViews() {
        author_edt = (EditText) findViewById(R.id.author_txt_id);
        title_edt = (EditText) findViewById(R.id.title_txt_id);
        info_edt = (EditText) findViewById(R.id.tour_info_txt_id);

        map_img = (ImageView) findViewById(R.id.map_img_id);
        map_img.setOnClickListener(mClickListener);
        tour_img_one = (ImageView) findViewById(R.id.img_1_id);
        tour_img_one.setOnClickListener(mClickListener);
        tour_img_one.setOnLongClickListener(mLongClickListener);
        tour_images.add(tour_img_one);
        tour_img_two = (ImageView) findViewById(R.id.img_2_id);
        tour_img_two.setOnClickListener(mClickListener);
        tour_img_two.setOnLongClickListener(mLongClickListener);
        tour_images.add(tour_img_two);
        tour_img_tree = (ImageView) findViewById(R.id.img_3_id);
        tour_img_tree.setOnClickListener(mClickListener);
        tour_img_tree.setOnLongClickListener(mLongClickListener);
        tour_images.add(tour_img_tree);
        tour_img_four = (ImageView) findViewById(R.id.img_4_id);
        tour_img_four.setOnClickListener(mClickListener);
        tour_img_four.setOnLongClickListener(mLongClickListener);
        tour_images.add(tour_img_four);
        tour_img_five = (ImageView) findViewById(R.id.img_5_id);
        tour_img_five.setOnClickListener(mClickListener);
        tour_img_five.setOnLongClickListener(mLongClickListener);
        tour_images.add(tour_img_five);

        save_btn = (Button) findViewById(R.id.save_tour_btn_id);
        save_btn.setOnClickListener(mClickListener);
        upload_btn = (Button) findViewById(R.id.upload_tour_btn_id);
        upload_btn.setOnClickListener(mClickListener);
        clear_btn = (Button) findViewById(R.id.clear_current_btn_id);
        clear_btn.setOnClickListener(mClickListener);
    }

    private void initInterfaces() {
        mClickListener = new View.OnClickListener() {
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
                                            SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.remove("locations");
                                            editor.commit();

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

                        break;

                    case R.id.map_img_id:
                        SharedPreferences sharedPreferences = getSharedPreferences(LocationUpdateService.sSharedPrefForFixedLocations, MODE_PRIVATE);
                        String current_trail = sharedPreferences.getString("locations", "");
                        if (current_trail.isEmpty()) {
                            Toast.makeText(getApplication(), "No current trail to show!!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent intent = new Intent(getApplication(), MapsActivity.class);
                        intent.putExtra("trail", current_trail);
                        startActivity(intent);
                        break;
                    case R.id.img_1_id:
                    case R.id.img_2_id:
                    case R.id.img_3_id:
                    case R.id.img_4_id:
                    case R.id.img_5_id:
                        getImages();
                        break;

                    default:
                        break;
                }
            }
        };

        mLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.img_1_id:
                        if (mImage_uris.size() == 0)
                            return true;
                        mImage_uris.remove(0);
                        setImageViewsSrc();
                        return true;
                    case R.id.img_2_id:
                        if (mImage_uris.size() < 2)
                            return true;
                        mImage_uris.remove(1);
                        setImageViewsSrc();
                        return true;
                    case R.id.img_3_id:
                        if (mImage_uris.size() < 3)
                            return true;
                        mImage_uris.remove(2);
                        setImageViewsSrc();
                        return true;
                    case R.id.img_4_id:
                        if (mImage_uris.size() < 4)
                            return true;
                        mImage_uris.remove(3);
                        setImageViewsSrc();
                        return true;
                    case R.id.img_5_id:
                        if (mImage_uris.size() < 5)
                            return true;
                        mImage_uris.remove(4);
                        setImageViewsSrc();
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options_activity_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            switch (requestCode) {
                case LocationUpdateService.REQUEST_CODE_FOR_RESOLUTION_REQUEST:
                    Toast.makeText(this, "If you enabled settings, try to start tracking again!!", Toast.LENGTH_LONG).show();
                    break;
                case INTENT_REQUEST_GET_IMAGES:
                    if (mImage_uris.size() == 0) {
                        mImage_uris = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
                        setImageViewsSrc();
                    } else {
                        ArrayList<Uri> temp = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);
                        mImage_uris.addAll(temp);
                        setImageViewsSrc();
                    }
                    break;
                default:
                    break;

            }
        }
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

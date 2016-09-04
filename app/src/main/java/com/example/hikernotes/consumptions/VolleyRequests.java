package com.example.hikernotes.consumptions;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by John on 8/27/2016.
 */
public class VolleyRequests {
    public static String sUrlForConnectivityCheck = "http://hikingapp.net23.net/checknetaccess.php";
    public static String sUrlForDataUpdate = "http://hikingapp.net23.net/updateappdata.php";
    public static String sUrlForVoting = "http://hikingapp.net23.net/vote.php";
    public static String sUrlForTourDetails = "http://hikingapp.net23.net/getremainingdata.php";
    public static String sUrlForNewTourAdd = "http://hikingapp.net23.net/addnewtour.php";
    public static String sUrlForImageUploads = "http://hikingapp.net23.net/storetourimages.php";
    public static String sUrlForNewComment = "http://hikingapp.net23.net/addnewcomment.php";
    public static String sUrlForPullingComments = "http://hikingapp.net23.net/pullcomments.php";
    public static String sUrlForOnMapImagesUpload = "http://hikingapp.net23.net/addonmapimages.php";

    public static RequestQueue sQueue = null;

    private VolleyRequests() {
    }

    public static RequestQueue getQueue(Context context) {
        if (null == sQueue)
            sQueue = Volley.newRequestQueue(context);
        return sQueue;
    }
}

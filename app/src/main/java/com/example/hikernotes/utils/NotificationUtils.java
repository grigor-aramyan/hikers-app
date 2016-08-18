package com.example.hikernotes.utils;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.hikernotes.MainActivity;
import com.example.hikernotes.R;
import com.example.hikernotes.activities.AddActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by John on 8/17/2016.
 */
public class NotificationUtils {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void showUpdatedLocationNotification(Context context, double latitude, double longitude) {
        int notificationID = 21;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_location);
        if (latitude != 0.0){
            remoteViews.setTextViewText(R.id.latitude_txt_id, "Lat: " + latitude);
        } else {
            remoteViews.setTextViewText(R.id.latitude_txt_id, "Lat: xxx");
        }
        if (longitude != 0.0){
            remoteViews.setTextViewText(R.id.longitude_txt_id, "Long: " + longitude);
        }else {
            remoteViews.setTextViewText(R.id.longitude_txt_id, "Long: xxx");
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        remoteViews.setTextViewText(R.id.time_txt_id, "Fix time " + simpleDateFormat.format(new Date()));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setAutoCancel(true);
        builder.setContentTitle("Location Fix");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(new Intent(context, AddActivity.class));

        builder.setContentIntent(stackBuilder.getPendingIntent(notificationID, 0));

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationID, builder.build());
    }
}

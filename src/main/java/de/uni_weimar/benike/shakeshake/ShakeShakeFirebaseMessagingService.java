package de.uni_weimar.benike.shakeshake;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class ShakeShakeFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        //Calling method to generate notification
        //sendNotification(remoteMessage.getNotification().getBody());(RefreshTask.REFRESH_DATA_INTENT));
        sendNotification(body);

        sendBroadcast(new Intent(StateTransition.UPDATE_STATE));
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                //PendingIntent.FLAG_ONE_SHOT);
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("ShakeShake Warning")
                .setSmallIcon(R.drawable.app_icon)
                //.setLargeIcon(R.drawable.app_icon)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[0]);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();
        //startForeground(1, notification);
        notificationManager.notify(1, notification);


    }
}

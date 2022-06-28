package com.example.shoestoreapp.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.shoestoreapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Heads Up Notification",
                NotificationManager.IMPORTANCE_HIGH
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("")
                .setSmallIcon(R.drawable.logo1)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(1, notification.build());
        super.onMessageReceived(message);
    }
}

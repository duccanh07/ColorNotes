package com.fivestars.colornotes.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fivestars.colornotes.LoadScreen;
import com.fivestars.colornotes.MainActivity;
import com.fivestars.colornotes.R;
import com.fivestars.colornotes.model.Note;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class AlarmBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String nTitle = "Ghi chú " + "\""+ bundle.getString("event") + "\"" + " đã đến hạn!";
        String dateTime = bundle.getString("dateTime");


        Intent intent1 = new Intent(context, LoadScreen.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final int id = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent1,PendingIntent.FLAG_ONE_SHOT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001");

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.expired);
        contentView.setTextViewText(R.id.message, nTitle);
        contentView.setTextViewText(R.id.date, dateTime);
        mBuilder.setSmallIcon(R.drawable.ic_baseline_event_note__white_24);
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(false);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.build().flags = Notification.FLAG_NO_CLEAR | Notification.PRIORITY_HIGH;
        mBuilder.setContent(contentView);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        Notification notification = mBuilder.build();
        notificationManager.notify(id, notification);

    }
}

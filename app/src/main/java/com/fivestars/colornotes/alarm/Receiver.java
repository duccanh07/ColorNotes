package com.fivestars.colornotes.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fivestars.colornotes.LoadScreen;
import com.fivestars.colornotes.MainActivity;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, LoadScreen.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);
    }
}

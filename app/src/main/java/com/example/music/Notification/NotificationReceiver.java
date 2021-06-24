package com.example.music.Notification;

import  android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.music.services.MusicService;

import static com.example.music.Notification.ApplicationClass.ACTION_NEXT;
import static com.example.music.Notification.ApplicationClass.ACTION_PLAY;
import static com.example.music.Notification.ApplicationClass.ACTION_PREV;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context, MusicService.class);
        if (actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName", "PlayPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREV:
                    serviceIntent.putExtra("ActionName", "Previous");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName", "Next");
                    context.startService(serviceIntent);
                    break;
            }
        }
    }
}

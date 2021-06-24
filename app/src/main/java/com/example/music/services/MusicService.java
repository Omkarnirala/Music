package com.example.music.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.music.utlities.ActionPlay;
import com.example.music.utlities.MusicFiles;

import java.util.ArrayList;

import static com.example.music.PlaySong.listOfSongs;

public class MusicService extends Service{
    IBinder iBinder = new MyBinder();
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    ActionPlay playSong;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        musicFiles = listOfSongs;
    }

    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String actionName = intent.getStringExtra("ActionName");

        if (actionName != null){
            switch (actionName){
                case "PlayPause":

                    if (playSong != null){
                        playSong.playPause();
                    }
                    break;
                case "Previous":

                    if (playSong != null ){
                        playSong.previous();
                    }
                    break;
                case "Next":

                    if (playSong != null ){
                        playSong.next();
                    }
                    break;
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void setCallBack(ActionPlay playSong){
        this.playSong = playSong;
    }
}

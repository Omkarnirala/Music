package com.example.music;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PlaySong extends AppCompatActivity {

    SeekBar seekbar;
    TextView songTitle, curTime, totTime;
    ImageView playIcon, prevIcon, nextIcon, repeatIcon, suffleIcon;
    ArrayList<File> mySongs;
    static MediaPlayer mediaPlayer;
    int position;
    String textContent;
    boolean isShuffle = false;
    boolean isRepeat = false;
    private static final String CHANNEL_ID = "Channel1";

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);

        seekbar = findViewById(R.id.mSeekBar);
        songTitle = findViewById(R.id.songTitle);
        curTime = findViewById(R.id.curTime);
        totTime = findViewById(R.id.totalTime);

        playIcon = findViewById(R.id.playIcon);
        prevIcon = findViewById(R.id.prevIcon);
        nextIcon = findViewById(R.id.nextIcon);
        repeatIcon = findViewById(R.id.repeatIcon);
        suffleIcon = findViewById(R.id.suffleIcon);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySongs = (ArrayList) bundle.getParcelableArrayList("songList");
        position = bundle.getInt("position",0);
        initPlayer(position);
        createNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        prevIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position <= 0) {
                    position = mySongs.size() - 1;
                } else {
                    position--;
                }

                initPlayer(position);
            }
        });


        nextIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < mySongs.size() - 1) {
                    position++;
                } else {
                    position = 0;
                }
                initPlayer(position);
            }
        });

        repeatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    repeatIcon.setImageResource(R.drawable.repeat_default);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    repeatIcon.setImageResource(R.drawable.repeat_all);
                    suffleIcon.setImageResource(R.drawable.suffle_default);
                }
            }
        });

        suffleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF" , Toast.LENGTH_SHORT).show();
                    suffleIcon.setImageResource(R.drawable.suffle_default);
                }else{
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    suffleIcon.setImageResource(R.drawable.suffle_on);
                    repeatIcon.setImageResource(R.drawable.repeat_default);
                }
            }
        });


    }

    private void initPlayer(int position) {

        if (mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.reset();
        }
        textContent =mySongs.get(position).getName().replace(".mp3","");
        songTitle.setText(textContent);
        songTitle.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());

        mediaPlayer =mediaPlayer.create( getApplicationContext(),uri );
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                String totalTime = createTimeLabel(mediaPlayer.getDuration());
                totTime.setText(totalTime);
                seekbar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int curSongPoition = position;
                // check for repeat is ON or OFF

                if (isRepeat){
                    // repeat is on play same song again

                    initPlayer(curSongPoition);

                }else if (isShuffle){
                    // shuffle is on - play a random song

                    Random rand = new Random();
                    curSongPoition = rand.nextInt(mySongs.size());
                    initPlayer(curSongPoition);

                }else {
                    // no repeat or shuffle ON - play next song

                    if (curSongPoition < mySongs.size() - 1) {
                        curSongPoition++;
                        initPlayer(curSongPoition);
                    } else {
                        curSongPoition = 0;
                        initPlayer(curSongPoition);
                    }
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                    seekbar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
//                        Log.i("Thread ", "Thread Called");
                        // create new message to send to handler
                        if (mediaPlayer.isPlaying()) {
                            Message msg = new Message();
                            msg.what = mediaPlayer.getCurrentPosition();
                            handler.sendMessage(msg);
                            Thread.sleep(800);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Log.i("handler ", "handler called");
            int current_position = msg.what;
            seekbar.setProgress(current_position);
            String cTime = createTimeLabel(current_position);
            curTime.setText(cTime);
        }
    };

    private void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            pause();
        }
    }

    private void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    public String createTimeLabel(int duration) {
        String timeLabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timeLabel += min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                // Add media control buttons that invoke intents in your media service
//                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", prevPendingIntent) // #0
//                .addAction(R.drawable.ic_pause_black_24dp, "Pause", pausePendingIntent)  // #1
//                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", nextPendingIntent)     // #2
                .setSmallIcon(R.drawable.music)
                .setContentTitle(songTitle.getText())
                .setContentText("Playing")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());

    }
}
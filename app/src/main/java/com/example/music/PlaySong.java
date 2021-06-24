package com.example.music;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.example.music.Notification.NotificationReceiver;
import com.example.music.services.MusicService;
import com.example.music.utlities.ActionPlay;
import com.example.music.utlities.MusicFiles;

import java.util.ArrayList;
import java.util.Random;

import static com.example.music.Notification.ApplicationClass.ACTION_NEXT;
import static com.example.music.Notification.ApplicationClass.ACTION_PLAY;
import static com.example.music.Notification.ApplicationClass.ACTION_PREV;
import static com.example.music.Notification.ApplicationClass.CHANNEL_ID;
import static com.example.music.Adapter.MusicAdapter.mFiles;

public class PlaySong extends AppCompatActivity implements ServiceConnection, ActionPlay {

    SeekBar seekbar;
    TextView songTitle, songArtist, curTime, totTime;
    ImageView playIcon;
    ImageView imageView, prevIcon, nextIcon, repeatIcon, shuffleIcon;
    public static ArrayList<MusicFiles> listOfSongs = new ArrayList<>();
    static MediaPlayer mediaPlayer;
    static Uri uri;
    int position = -1;
    String textContent, textContent2;
    boolean isShuffle = false;
    boolean isRepeat = false;
    MusicService musicService;
    MediaSessionCompat mediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play);


        seekbar = findViewById(R.id.mSeekBar);
        songTitle = findViewById(R.id.songTitle);
        curTime = findViewById(R.id.curTime);
        totTime = findViewById(R.id.totalTime);
        songArtist = findViewById(R.id.songArtist);

        imageView = findViewById(R.id.imageView);
        playIcon = findViewById(R.id.playIcon);
        prevIcon = findViewById(R.id.prevIcon);
        nextIcon = findViewById(R.id.nextIcon);
        repeatIcon = findViewById(R.id.repeatIcon);
        shuffleIcon = findViewById(R.id.suffleIcon);

        mediaSession = new MediaSessionCompat(this, "Music");
        position = getIntent().getIntExtra("position", -1);
        listOfSongs = mFiles;
        initPlayer(position);

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });

        prevIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
            }
        });

        nextIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        repeatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    repeatIcon.setImageResource(R.drawable.repeat_default);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    repeatIcon.setImageResource(R.drawable.repeat_all);
                    shuffleIcon.setImageResource(R.drawable.suffle_default);
                }
            }
        });

        shuffleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    shuffleIcon.setImageResource(R.drawable.suffle_default);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    shuffleIcon.setImageResource(R.drawable.suffle_on);
                    repeatIcon.setImageResource(R.drawable.repeat_default);
                }
            }
        });
    }

    public void playPause() {

        if (mediaPlayer != null && ! mediaPlayer.isPlaying()) {
            createNotification(R.drawable.ic_pause_black_24dp);
            playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
            uri = Uri.parse(listOfSongs.get(position).getPath());
            mediaPlayer.start();

        } else if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            createNotification(R.drawable.ic_play_arrow_black_24dp);
            playIcon.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    public void next() {
        if ((position +1) < listOfSongs.size()) {
            position++;
        } else {
            position = 0;
        }
        initPlayer(position);
    }

    public void previous() {
        if (position <= 0) {
            position = listOfSongs.size() - 1;
        } else {
            position--;
        }
        initPlayer(position);
    }

    private void initPlayer(int position) {

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }
        uri = Uri.parse(listOfSongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();

        textContent = listOfSongs.get(position).getTitle();
        textContent2 = listOfSongs.get(position).getArtist();
        songTitle.setText(textContent);
        songTitle.setSelected(true);
        songArtist.setText(textContent2);
        songArtist.setSelected(true);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art = retriever.getEmbeddedPicture();
        if (art != null) {
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(imageView);

        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(imageView);
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                String totalTime = createTimeLabel(mediaPlayer.getDuration());
                totTime.setText(totalTime);
                seekbar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                createNotification(R.drawable.ic_pause_black_24dp);
                playIcon.setImageResource(R.drawable.ic_pause_black_24dp);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int curSongPosition = position;
                // check for repeat is ON or OFF

                if (isRepeat) {
                    // repeat is on play same song again

                    initPlayer(curSongPosition);

                } else if (isShuffle) {
                    // shuffle is on - play a random song

                    Random rand = new Random();
                    curSongPosition = rand.nextInt(listOfSongs.size());
                    initPlayer(curSongPosition);

                } else {
                    // no repeat or shuffle ON - play next song

                    if (curSongPosition < listOfSongs.size() - 1) {
                        curSongPosition++;
                        initPlayer(curSongPosition);
                    } else {
                        curSongPosition = 0;
                        initPlayer(curSongPosition);
                    }
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
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
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int current_position = msg.what;
            seekbar.setProgress(current_position);
            String cTime = createTimeLabel(current_position);
            curTime.setText(cTime);
        }
    };

    @Override
    protected void onPause() {

        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
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

    private void createNotification(int playPause) {

        Intent intent = new Intent(this, PlaySong.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREV);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(listOfSongs.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.music);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(playPause)
                .setLargeIcon(thumb)
                .setContentTitle(listOfSongs.get(position).getTitle())
                .setContentText(listOfSongs.get(position).getArtist())
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", prevPendingIntent) // #0
                .addAction(playPause, "Pause", pausePendingIntent)  // #1
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", nextPendingIntent)     // #2
                .setSmallIcon(R.drawable.logo)
                // Apply the media style template
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().
                        setMediaSession(mediaSession.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());

    }

    public byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelAllNotification(getApplicationContext());
    }

    private void cancelAllNotification(Context applicationContext) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) applicationContext.getSystemService(ns);
        nMgr.cancelAll();
    }
}
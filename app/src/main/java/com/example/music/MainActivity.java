 package com.example.music;

 import android.Manifest;
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.SearchView;
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.ContextCompat;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.example.music.Adapter.MusicAdapter;
 import com.example.music.utlities.MusicFiles;

 import org.jetbrains.annotations.NotNull;

 import java.util.ArrayList;

 public class MainActivity extends AppCompatActivity {

     private static final int REQUEST_CODE = 1;
     static ArrayList<MusicFiles> musicFiles;
     RecyclerView recyclerView;
     MusicAdapter musicAdapter;


     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         getMenuInflater().inflate(R.menu.search,menu);
         MenuItem menuItem = menu.findItem(R.id.searchOption);
         SearchView searchView = (SearchView)menuItem.getActionView();
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 return false;
             }

             @Override
             public boolean onQueryTextChange(String newText) {
                 String userInput = newText.toLowerCase();
                 ArrayList<MusicFiles> myFiles = new ArrayList<>();
                 for (MusicFiles Song: musicFiles){
                     if (Song.getTitle().toLowerCase().contains(userInput) || Song.getAlbum().toLowerCase().contains(userInput)){
                         myFiles.add(Song);
                     }
                 }
                 musicAdapter.UpdateList(myFiles);
                 return true;

             }
         });
         return true;
     }

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         permission();

         recyclerView = findViewById(R.id.recyclerView);
         recyclerView.setHasFixedSize(true);
         recyclerView.setNestedScrollingEnabled(false);
         if (!(musicFiles.size() <1 ))
         {
             musicAdapter = new MusicAdapter(getApplicationContext(),musicFiles);
             recyclerView.setAdapter(musicAdapter);
             recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),RecyclerView.VERTICAL,false));
         }
     }

     private void permission() {
         if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 != PackageManager.PERMISSION_GRANTED) {

             ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
         } else {

            //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
             musicFiles = getAllAudio(this);

         }
     }

     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);

         if (requestCode == REQUEST_CODE) {

             if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                 //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                 musicFiles = getAllAudio(this);

             } else {

                 ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
             }
         }
     }

     public static ArrayList<MusicFiles> getAllAudio(Context context) {
         ArrayList<MusicFiles> mySongs = new ArrayList<>();

         Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
         String[] projection = {
                 MediaStore.Audio.Media.ALBUM,
                 MediaStore.Audio.Media.TITLE,
                 MediaStore.Audio.Media.DURATION,
                 MediaStore.Audio.Media.DATA, //for path
                 MediaStore.Audio.Media.ARTIST
         };

         Cursor cursor = context.getContentResolver().query(uri,projection,
                 null,null,null);
         if (cursor != null){
             while (cursor.moveToNext()){
                 String album = cursor.getString(0);
                 String title = cursor.getString(1);
                 String duration = cursor.getString(2);
                 String path = cursor.getString(3);
                 String artist = cursor.getString(4);

                MusicFiles musicFiles = new MusicFiles(path,title,artist,album,duration);

                if (musicFiles.getPath().endsWith("mp3") && !musicFiles.getPath().startsWith(".")){
                    mySongs.add(musicFiles);
                    Log.e("Music" + musicFiles, "songs" +mySongs);
                }
             }
             cursor.close();
         }
         return mySongs;
     }
 }
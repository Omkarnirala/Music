 package com.example.music;

 import android.Manifest;
 import android.app.NotificationChannel;
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;

 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.NotificationCompat;
 import androidx.core.app.NotificationManagerCompat;

 import com.karumi.dexter.Dexter;
 import com.karumi.dexter.PermissionToken;
 import com.karumi.dexter.listener.PermissionDeniedResponse;
 import com.karumi.dexter.listener.PermissionGrantedResponse;
 import com.karumi.dexter.listener.PermissionRequest;
 import com.karumi.dexter.listener.single.PermissionListener;

 import java.io.File;
 import java.util.ArrayList;

 public class MainActivity extends AppCompatActivity {
    ListView listView;
    String[] items;
     String songTitle = "";
    ArrayAdapter<String> arrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

//                        Toast.makeText(MainActivity.this, "Runtime permission given", Toast.LENGTH_SHORT).show();
                        ArrayList<File> mySongs = fetchSongs(Environment.getExternalStorageDirectory());
                        items = new String[mySongs.size()];
                        for (int i = 0; i < mySongs.size(); i++) {
                            items[i] = mySongs.get(i).getName().replace(".mp3", "");

                        }


                        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
                        listView.setAdapter(arrayAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                                Intent intent = new Intent(MainActivity.this, PlaySong.class);
                                String mycurrentSong = listView.getItemAtPosition(position).toString();
                                intent.putExtra("songList", mySongs);
                                intent.putExtra("currentSong", mycurrentSong);
                                intent.putExtra("position", position);
                                startActivity(intent);


                            }
                        });


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }



    public ArrayList<File> fetchSongs(File file){
        ArrayList arrayList = new ArrayList();
        File [] songs = file.listFiles();
        if(songs !=null){
            for(File myFile: songs){
                if(!myFile.isHidden() && myFile.isDirectory()){
                    arrayList.addAll(fetchSongs(myFile));
                }
                else{
                    if(myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".")){
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;
    }
}
package com.example.music;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private final Context mContext;
    static ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles) {
        this.mContext = mContext;
        this.mFiles = mFiles;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MusicAdapter.MyViewHolder holder, int position) {

        holder.file_Name.setText(mFiles.get(position).getTitle());
        holder.artist_Name.setText(mFiles.get(position).getArtist());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null){
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.song_Thumbnail);

        }else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(holder.song_Thumbnail);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//              Toast.makeText(mContext, "Working : " + mFiles.get(position).getPath(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mContext, PlaySong.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("Song Images", image);
                intent.putExtra("position", position);
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView file_Name;
        TextView artist_Name;
        ImageView song_Thumbnail;

        public MyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            file_Name = itemView.findViewById(R.id.musicFileName);
            artist_Name =itemView.findViewById(R.id.artistName);
            song_Thumbnail = itemView.findViewById(R.id.songImage);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void UpdateList(ArrayList<MusicFiles> musicFilesArrayList){

        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }

}

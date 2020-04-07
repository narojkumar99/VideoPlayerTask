package com.videoplayertask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;


import com.videoplayertask.adapter.VideoItemAdapter;
import com.videoplayertask.interfaces.ClickListener;
import com.videoplayertask.models.VideoModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClickListener {
    private static final int PERMISSION_CODE = 101;
    private RecyclerView video_list;
    private HashSet<String> hashSet = new HashSet<>();
    private List<String> file_path;
    private List<VideoModel> videoModelList;
    private VideoItemAdapter videoItemAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initViews();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                readAllFIles();
            } else {
                requestPermission();
            }
        } else {
            readAllFIles();
        }
    }

    private void initViews() {
        video_list = findViewById(R.id.video_list);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Allow Permission", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readAllFIles();
                }
        }
    }

    private void readAllFIles() {
        String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    hashSet.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        file_path = new ArrayList<>(hashSet);
        videoModelList = new ArrayList<>();
        for (String data : file_path) {
            File file = new File(data);
            videoModelList.add(new VideoModel(file.getName(), file.getAbsolutePath()));
        }
        video_list.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        videoItemAdapter = new VideoItemAdapter(videoModelList, MainActivity.this, MainActivity.this);
        video_list.setAdapter(videoItemAdapter);
    }

    @Override
    public void onClickItem(String filePath) {
        startActivity(new Intent(MainActivity.this, VideoPlayerActivtit.class).putExtra("path_file", filePath));
    }
}

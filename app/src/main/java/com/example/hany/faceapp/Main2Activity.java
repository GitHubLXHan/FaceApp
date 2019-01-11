package com.example.hany.faceapp;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Uri uri = Uri.parse("content://com.example.hany.wechat.Content.MyContentProvider/Near");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imgName = cursor.getString(cursor.getColumnIndex("imgName"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String summary = cursor.getString(cursor.getColumnIndex("summary"));
                String TAG = "ContentInformation";
                Log.d(TAG, "imgName: " + imgName);
                Log.d(TAG, "name: " + name);
                Log.d(TAG, "time: " + time);
                Log.d(TAG, "summary: " + summary);
            }
        }
    }
}

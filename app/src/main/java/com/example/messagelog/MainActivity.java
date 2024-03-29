 package com.example.messagelog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

 public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
     private static final int REQUEST_CODE_PERMISSION_READ_SMS = 101;
     private Adapter messageLogAdapter;
     private List<Model> messageLogDataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=findViewById(R.id.activity_main_rv);
        swipeRefreshLayout=findViewById(R.id.activity_main_swipe_refresh_layout);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageLogDataList = new ArrayList<>();
        messageLogAdapter = new Adapter(messageLogDataList);
        recyclerView.setAdapter(messageLogAdapter);
        
        fetchMessageLogs();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check for permission
                    fetchMessageLogs();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

     private void fetchMessageLogs() {
         if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
                 != PackageManager.PERMISSION_GRANTED) {
             ActivityCompat.requestPermissions(this,
                     new String[]{android.Manifest.permission.READ_SMS},
                     REQUEST_CODE_PERMISSION_READ_SMS);
         } else {
             queryMessageLogs();
         }
     }

     @SuppressLint("NotifyDataSetChanged")
     private void queryMessageLogs() {
         messageLogDataList.clear();

         Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI,
                 null,
                 null,
                 null,
                 Telephony.Sms.DEFAULT_SORT_ORDER + " LIMIT 20");

         if (cursor != null && cursor.moveToFirst()) {
             do {
                 @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                 @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                 @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE));

                 Model messageLogData = new Model(address, body, date);
                 messageLogDataList.add(messageLogData);
             } while (cursor.moveToNext());
             cursor.close();
             messageLogAdapter.notifyDataSetChanged();
         } else {
             Toast.makeText(this, "No message logs found", Toast.LENGTH_SHORT).show();
         }
     }

     @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         super.onRequestPermissionsResult(requestCode, permissions, grantResults);
             if (requestCode == REQUEST_CODE_PERMISSION_READ_SMS &&
                     grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 queryMessageLogs();
             } else {
                 Toast.makeText(this, "Permission denied. Cannot fetch message logs.", Toast.LENGTH_SHORT).show();
             }
     }
}
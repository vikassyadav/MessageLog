 package com.example.messagelog;

import androidx.annotation.NonNull;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

 public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    Button b1;
     private static final int REQUEST_CODE_PERMISSION_READ_SMS = 101;
     private Adapter messageLogAdapter;
     private List<Model> messageLogDataList;
     private FirebaseFirestore firestore;

     @SuppressLint("MissingInflatedId")
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=findViewById(R.id.activity_main_rv);
        swipeRefreshLayout=findViewById(R.id.activity_main_swipe_refresh_layout);
        b1=findViewById(R.id.fetchFromFb);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageLogDataList = new ArrayList<>();
        messageLogAdapter = new Adapter(messageLogDataList);
        recyclerView.setAdapter(messageLogAdapter);

        firestore = FirebaseFirestore.getInstance();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , fetch.class);
                startActivity(intent);
            }
        });


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
             // Upload data to Firestore
             uploadDataToFirestore(messageLogDataList);
         } else {
             Toast.makeText(this, "No message logs found", Toast.LENGTH_SHORT).show();
         }
     }
     private void uploadDataToFirestore(List<Model> messageLogs) {
         // Get a reference to the Firestore collection
         CollectionReference messageLogCollection = firestore.collection("MessageLogs");

         // Query existing data to compare with new data
         messageLogCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 // Iterate through the new data
                 for (Model messageLog : messageLogs) {
                     // Check if the new data already exists in Firestore
                     boolean newDataExists = false;
                     for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                         Model existingMessageLog = document.toObject(Model.class);
                         if (existingMessageLog.equals(messageLog)) {
                             newDataExists = true;
                             break;
                         }
                     }

                     // If the new data doesn't exist, add it to Firestore
                     if (!newDataExists) {
                         // Create a Map object to store the message log data
                         Map<String, Object> data = new HashMap<>();
                         data.put("address", messageLog.getAddress());
                         data.put("body", messageLog.getBody());
                         data.put("date", messageLog.getDate());

                         // Add the data to Firestore
                         messageLogCollection.add(data)
                                 .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                     @Override
                                     public void onComplete(@NonNull Task<DocumentReference> task) {
                                         if (task.isSuccessful()) {
                                             Toast.makeText(MainActivity.this, "New message log uploaded to Firestore", Toast.LENGTH_SHORT).show();
                                         } else {
                                             Toast.makeText(MainActivity.this, "Failed to upload new message log to Firestore", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                 });
                     }
                 }
             }
         });
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
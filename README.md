# Message Log App

## Overview
The Message Log App is an Android application that allows users to view and manage their message logs. It retrieves message logs from the device's SMS database and displays them in a user-friendly interface. Additionally, it provides functionality to upload message logs to Firebase Firestore and retrieve them from there.

## Features
- View and manage message logs locally on the device.
- Upload message logs to Firebase Firestore.
- Retrieve message logs from Firebase Firestore.

## How to Use
### Fetching Message Logs
When the app is launched, it automatically fetches the latest 20 message logs from the device's SMS database. Users can refresh the message log list by pulling down on the screen.

### Uploading to Firestore
Users can upload their message logs to Firebase Firestore by clicking on the "Upload to Firestore" button. This sends the message log data to the Firestore database.

### Retrieving from Firestore
To retrieve message logs from Firebase Firestore, users can navigate to the "Fetch from Firestore" activity. This activity fetches the message logs stored in Firestore and displays them in the app.

## Required Methods
- `fetchMessageLogs()`: Fetches message logs from the device's SMS database.
- `queryMessageLogs()`: Queries the SMS content provider to retrieve message logs.
- `uploadDataToFirestore()`: Uploads message logs to Firebase Firestore.
- `fetchDataFromFirestore()`: Retrieves message logs from Firebase Firestore.

#### Required Android Default Method
```java
private void fetchMessageLogs() {
    // Query the message log
    Uri uri = Uri.parse("content://sms");
    Cursor cursor = getContentResolver().query(
            uri,
            null,
            null,
            null,
            Telephony.Sms.DEFAULT_SORT_ORDER
    );
      // Process the message log data and add it to messageLogDataList
    if (cursor != null && cursor.moveToFirst()) {
        do {
            // Extract relevant information from the cursor
            String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
            String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
            String date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE));

            // Create a LogData object and add it to the list
            LogData logData = new LogData(address, body, date);
            messageLogDataList.add(logData);
        } while (cursor.moveToNext());
    }

    // Close the cursor
    if (cursor != null) {
        cursor.close();
    }

```


## Why Firestore?
Firebase Firestore is chosen over Firebase Realtime Database for the following reasons:
- **Scalability**: Firestore is more scalable and can handle larger datasets and more complex queries.
- **Querying**: Firestore provides powerful querying capabilities, including filtering, sorting, and limiting results.
- **Real-time Updates**: Firestore offers real-time synchronization, allowing the app to receive updates in real-time when data changes in the database.
- **Offline Support**: Firestore supports offline data persistence, enabling the app to function even when the device is offline.

By leveraging Firestore, the Message Log App can provide a more robust and scalable solution for storing and retrieving message logs, ensuring a better user experience.


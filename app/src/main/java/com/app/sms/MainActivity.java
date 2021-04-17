package com.app.sms;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    DriveServiceHelper driveServiceHelper;

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;

    final String welcomeScreenShownPref = "welcomeScreenShown";
    Button b1;
    Intent  intent;
    SharedPreferences mPrefs;
    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
        


//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.RECEIVE_SMS},
//                MY_PERMISSIONS_REQUEST_SMS_RECEIVE);



//        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
//        filter.setPriority(999);
//        registerReceiver(smsReceiver, filter);


        b1= findViewById(R.id.b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent,202);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean(welcomeScreenShownPref, true);
                editor.commit();

            }
        });

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        intent = new Intent(this,SMSService.class);
        // second argument is the default to use if the preference can't be found
        Boolean welcomeScreenShown = mPrefs.getBoolean(welcomeScreenShownPref, false);
        if(!welcomeScreenShown){
            Toast.makeText(this,"Need SMS Permissions... Click Permissions -> Allow SMS",Toast.LENGTH_LONG).show();

            b1.callOnClick();

        }else{
            if(checkSelfPermission(Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Need SMS Permissions... Click Permissions -> Allow SMS",Toast.LENGTH_LONG).show();
                b1.callOnClick();
            }
            else{
                requestSignIn();
                b1.setText("All Good! New SMS will be pushed to Drive!");


            }
        }




    }





    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //here you can parse intent and get sms fields.

           // Toast.makeText(context,"Success 2",Toast.LENGTH_LONG).show();

//            readSMS();
//            uploadPdfFile();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(smsReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            // YES!!
            Log.i("TAG", "MY_PERMISSIONS_REQUEST_SMS_RECEIVE --> YES");
        }
    }
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

    private void requestSignIn() {

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();


        GoogleSignInClient client = GoogleSignIn.getClient(this,signInOptions);


        startActivityForResult(client.getSignInIntent(),0);
        Log.d("main","here 1");

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



                    if(requestCode ==0){
                        handleSignInIntent(data);
                    }

                    if(requestCode==202){
                        if(checkSelfPermission(Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){
                             Toast.makeText(this,"Permission Denied. Exiting...",Toast.LENGTH_LONG).show();
                            finish();
                        }else{
                            requestSignIn();
                            b1.setText("All Good! New SMS will be pushed to Drive!");


                        }
                    }


    }

    private void handleSignInIntent(Intent data) {

        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(MainActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE)) ;

                credential.setSelectedAccount(googleSignInAccount.getAccount());

                Drive googleDriveService = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential
                ).setApplicationName("My Drive Tutorial")
                        .build();

                driveServiceHelper = new DriveServiceHelper(googleDriveService);

                SMSService.driveServiceHelper =driveServiceHelper;
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("drive",driveServiceHelper);
//                intent.putExtras(bundle);
//                // intent.putExtras(bundle);
                startService(intent);


            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }




    public void uploadPdfFile(){
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);

        pd.setTitle("uploading");
        pd.setMessage("wait");
        pd.show();
        java.io.File path = getApplicationContext().getFilesDir();

        String filePath = path.toString() +"/config.txt";
        //Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_LONG).show();
        driveServiceHelper.createFilePDF(filePath).addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
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
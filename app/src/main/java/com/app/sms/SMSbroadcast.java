package com.app.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class SMSbroadcast extends BroadcastReceiver {

    private static String SMS = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(SMS)){
            Bundle bundle = intent.getExtras();


        }


    }
}

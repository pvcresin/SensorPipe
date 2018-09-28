package com.example.leica.udp_3thread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

// http://android.keicode.com/basics/services-communicate-broadcast-receiver.php

public class ImageBroadcastReceiver extends BroadcastReceiver { // service onHandle -> broadcast -> full activity
    String TAG = "BroadcastReceiver";

    @Override public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        Bundle bundle = intent.getExtras();

        byte[] imgBytes = bundle.getByteArray("imgArray");
        int offset = bundle.getInt("imgOffset");
        int length = bundle.getInt("imgLength");

        FullActivity full = (FullActivity)context;

        full.setImg(imgBytes, offset,length);
    }

}
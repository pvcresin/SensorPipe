package com.example.leica.udp_3thread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class ShadowActivity extends Activity { //http://qiita.com/ryo-ma/items/68844abb821dc03e69c7

    String TAG = "UdpService";

    String mode = "noMode";
    String toastStr = "default";
    String dTitle = "Title", dMessage = "Message", dPositive = "Positive", dNegative = "Negative";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shadow);

        Intent myIntent = getIntent();

        mode = myIntent.getStringExtra("mode");

        switch (mode) {

            case "dialog":
                Log.d(TAG, "s-dialog");
                dTitle = myIntent.getStringExtra("title");
                dMessage = myIntent.getStringExtra("message");
                dPositive = myIntent.getStringExtra("positive");
                dNegative = myIntent.getStringExtra("negative");

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setTitle(dTitle);
                alertBuilder.setMessage(dMessage);

                alertBuilder.setPositiveButton(dPositive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(ShadowActivity.this, "PositiveButton", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alertBuilder.setNegativeButton(dNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(ShadowActivity.this, "NegativeButton", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alertBuilder.create().show();

                break;

            case "toast":
                Log.d(TAG, "s-toast");
                toastStr =  myIntent.getStringExtra("toastText");
                Toast.makeText(this, toastStr, Toast.LENGTH_SHORT).show();
                finish();
                break;


            case "background":
                Log.d(TAG, "s-background");
                break;

            case "sound":
                Log.d(TAG, "s-sound");
                break;

            default:
                Log.d(TAG, "s-default");
                break;
        }
        //finish();
    }

}

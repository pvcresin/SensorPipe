package com.example.leica.udp_3thread;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class FullActivity extends Activity {
    String TAG = "Full";

    String remote_IP, my_IP;
    int remote_port, my_port;

    MultiTouchView mtv;

    Bitmap bm = null;
    ImageView iv;

    byte[] rcvByte;
    ImageBroadcastReceiver receiver;
    IntentFilter intentFilter;      // filter to identify sender(=service)

    //GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        loadPref();

        mtv = (MultiTouchView) findViewById(R.id.multi);
        mtv.setFullActivity(FullActivity.this);

        //gestureDetector = new GestureDetector(gestureL);

        iv = (ImageView)findViewById(R.id.imageView);

        receiver = new ImageBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("IMG_ACTION");
        registerReceiver(receiver, intentFilter);
    }

    public void setImg(byte[] imgByte, int offset, int dataLength) {
        rcvByte = imgByte;

        bm = BitmapFactory.decodeByteArray(imgByte, offset, dataLength);

        Log.d(TAG, "byte[] from broadcast receiver , length = " + dataLength);

        runOnUiThread(new Runnable() {  // handle main ui thread Activity#runOnUiThread
            @Override
            public void run() {
                if (bm != null) {
                    Log.d("run on ui", "set bitmap");
                    iv.setImageBitmap(bm);
                    bm = null;
                }
            }
        });
    }

    public void loadPref() {
        try {
            SharedPreferences pref = getSharedPreferences("pref.txt", MODE_PRIVATE);
            remote_IP = pref.getString("remote_IP", "");
            remote_port = Integer.parseInt(pref.getString("remote_port", ""));
            my_IP = pref.getString("my_IP", "");
            my_port = Integer.parseInt(pref.getString("my_port", ""));

        } catch (Exception e) {
            Log.e(TAG, "cant load");
        }
    }

    GestureDetector.OnGestureListener gestureL = new GestureDetector.SimpleOnGestureListener() {

        public boolean onDown(MotionEvent e) {  // touch
            Log.d(TAG, "called onDown()");
            return false;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {  // flick = true
            Log.d(TAG, "called onFling()");
            return false;
        }

        public void onLongPress(MotionEvent e) {    // long tap
            Log.d(TAG, "called onLongPress()");
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // scroll = true
            Log.d(TAG, "called onScroll()");
            return false;
        }

        public void onShowPress(MotionEvent e) {    // touch except flick or scroll
            Log.d(TAG, "called onShowPress()");
        }

        public boolean onSingleTapUp(MotionEvent e) {   // single tap contain double tap
            Log.d(TAG, "called onSingleTapUp()");
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) { // double tap
            Log.d(TAG, "called onDoubleTap()");
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {    // tap, scroll or release ( after double tap )
            Log.d(TAG, "called onDoubleTapEvent()");
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {    // single tap except double tap
            Log.d(TAG, "called onSingleTapConfirmed()");
            return false;
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {   // identify motion event
        //gestureDetector.onTouchEvent(ev);

        return false;
    }

    public void sendValues(final String Message, final int len) {    // used by Multi touch View
        new Thread(new Runnable() {
            public void run() {
                try {
                    DatagramSocket sDS = new DatagramSocket();  //DatagramSocket 作成

                    InetAddress remoteHost = InetAddress.getByName(remote_IP);

                    ByteBuffer byteBuf = ByteBuffer.allocate(len);

                    byte[] dataP = Message.getBytes();

                    byteBuf.put(dataP);

                    byte[] sData = byteBuf.array();

                    DatagramPacket sDP = new DatagramPacket(sData, sData.length, remoteHost, remote_port);  //DatagramPacket

                    sDS.send(sDP);

                } catch (Exception e) {
                    System.err.println("Exception : " + e);
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);

        super.onDestroy();
    }
}

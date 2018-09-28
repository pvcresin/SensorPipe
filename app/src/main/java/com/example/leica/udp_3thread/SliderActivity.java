package com.example.leica.udp_3thread;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class SliderActivity extends Activity {

    String TAG = "Slider";

    String remote_IP, my_IP;
    int remote_port, my_port;

    TextView tv0, tv1, tv2, tv3, tv4, tv5, tv6, tv7;

    TextView[] tvs = {tv0, tv1, tv2, tv3};

    SeekBar sb0, sb1, sb2, sb3, sb4, sb5, sb6, sb7;

    SeekBar[] sbs = {sb0, sb1, sb2, sb3};

    String sendStr = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        loadPref();

        tv0 = (TextView) findViewById(R.id.stv0);
        tv1 = (TextView) findViewById(R.id.stv1);
        tv2 = (TextView) findViewById(R.id.stv2);
        tv3 = (TextView) findViewById(R.id.stv3);
        tv4 = (TextView) findViewById(R.id.stv4);
        tv5 = (TextView) findViewById(R.id.stv5);
        tv6 = (TextView) findViewById(R.id.stv6);
        tv7 = (TextView) findViewById(R.id.stv7);

        sb0 = (SeekBar) findViewById(R.id.s0);
        sb1 = (SeekBar) findViewById(R.id.s1);
        sb2 = (SeekBar) findViewById(R.id.s2);
        sb3 = (SeekBar) findViewById(R.id.s3);
        sb4 = (SeekBar) findViewById(R.id.s4);
        sb5 = (SeekBar) findViewById(R.id.s5);
        sb6 = (SeekBar) findViewById(R.id.s6);
        sb7 = (SeekBar) findViewById(R.id.s7);

        sb0.setProgress(50);
        sb1.setProgress(50);
        sb2.setProgress(50);
        sb3.setProgress(50);
        sb4.setProgress(50);
        sb5.setProgress(50);
        sb6.setProgress(50);
        sb7.setProgress(50);

        sb0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv0.setText("Slider" + 0 + " : " + progress);
                sendValues("Slider" + 0 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv1.setText("Slider" + 1 + " : " + progress);
                sendValues("Slider" + 1 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv2.setText("Slider" + 2 + " : " + progress);
                sendValues("Slider" + 2 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv3.setText("Slider" + 3 + " : " + progress);
                sendValues("Slider" + 3 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv4.setText("Slider" + 4 + " : " + progress);
                sendValues("Slider" + 4 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv5.setText("Slider" + 5 + " : " + progress);
                sendValues("Slider" + 5 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv6.setText("Slider" + 6 + " : " + progress);
                sendValues("Slider" + 6 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
        });

        sb7.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv7.setText("Slider" + 7 + " : " + progress);
                sendValues("Slider" + 7 + ":" + progress  + ",", 20);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {                }
            public void onStopTrackingTouch(SeekBar seekBar) {                }
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


    public void sendValues(final String Message, final int len) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    DatagramSocket sDS = new DatagramSocket();  //DatagramSocket 作成

                    InetAddress remoteHost = InetAddress.getByName(remote_IP);

                    ByteBuffer byteBuf = ByteBuffer.allocate(len);

                    byte[] dataP = Message.getBytes();

                    byteBuf.put(dataP);

                    //byteBuf.put( "Other:100,".getBytes() );

                    byte[] sData = byteBuf.array();

                    DatagramPacket sDP = new DatagramPacket(sData, sData.length, remoteHost, remote_port);  //DatagramPacket

                    sDS.send(sDP);

                } catch (Exception e) {
                    System.err.println("Exception : " + e);
                }
            }
        }).start();

    }

}

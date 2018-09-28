package com.example.leica.udp_3thread;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

// http://blog.livedoor.jp/tmako123-programming/archives/39486172.html
// http://blog.livedoor.jp/tmako123-programming/archives/41725589.html

public class MainActivity extends Activity {

    String remote_IP, my_IP, remote_port, my_port;

    EditText remoteIpEdit, remotePortEdit, myPortEdit;
    TextView myIpText;
    Button startBtn, endBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteIpEdit = (EditText)findViewById(R.id.remoteIpEdit);
        remotePortEdit = (EditText)findViewById(R.id.remotePortEdit);
        myPortEdit = (EditText)findViewById(R.id.myPortEdit);

        myIpText = (TextView)findViewById(R.id.myIpText);

        loadPref();

        startBtn = (Button)findViewById(R.id.serviceStartBtn);
        endBtn = (Button)findViewById(R.id.serviceEndButton);

        startBtn.setOnClickListener(btnListener);
        endBtn.setOnClickListener(btnListener);
    }

    @Override
    public void onResume(){
        super.onResume();

        loadPref();
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        public void onClick(View v) {

            switch(v.getId()){

                case R.id.serviceStartBtn:
                    savePref();

                    //Toast.makeText(MainActivity.this, "Main", Toast.LENGTH_SHORT).show();

                    Intent fullIntent = new Intent(MainActivity.this, FullActivity.class);

                    startActivity(fullIntent);

                    Intent intent = new Intent(MainActivity.this, UdpService.class);    // start service
                    startService(intent);

                    Log.d("main", "double intent");


                    break;

                case R.id.serviceEndButton:
                    stopService(new Intent(MainActivity.this, UdpService.class));
                    break;
            }
        }
    };

    public void loadPref(){
        try {
            SharedPreferences pref = getSharedPreferences("pref.txt", MODE_PRIVATE);
            remote_IP   = pref.getString("remote_IP", "");
            remote_port = pref.getString("remote_port", "");
            my_IP       = pref.getString("my_IP", "");
            my_port     = pref.getString("my_port", "");

            remoteIpEdit.setText(remote_IP);
            remotePortEdit.setText(remote_port);
            //myIpText.setText(my_IP);
            myPortEdit.setText(my_port);

            if (getLocalIP() != null) myIpText.setText(getLocalIP());

            //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

        } catch(Exception e){
            //Log.d("load file", "cant load");
        }
    }

    public void savePref(){   // https://akira-watson.com/android/sharedpreferences.html
        String rIP =   remoteIpEdit.getText().toString();
        String rPort = remotePortEdit.getText().toString();
        String mIP =   myIpText.getText().toString();
        String mPort = myPortEdit.getText().toString();

        if (rIP.equals("") || rPort.equals("") || mPort.equals("")) {        // dont save
            Toast.makeText(this, "space", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            SharedPreferences pref = getSharedPreferences("pref.txt", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            // key, value
            editor.putString("remote_IP", rIP);
            editor.putString("remote_port", rPort);
            editor.putString("my_IP", mIP);
            editor.putString("my_port", mPort);
            editor.apply();

        } catch(Exception e){
            Log.e("file input", "error");
        }
    }

    String getLocalIP(){
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, FullActivity.class));   // full
            return true;
        } else if (id == R.id.slider_item) {
            startActivity(new Intent(MainActivity.this, SliderActivity.class)); // slider
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
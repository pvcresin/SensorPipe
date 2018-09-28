package com.example.leica.udp_3thread;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

// http://dev.classmethod.jp/smartphone/android/android-tips-home-alert-dialog/

public class UdpService extends IntentService implements SensorEventListener, LocationListener {

    String TAG = "UdpService";


    String remote_IP, my_IP;
    int remote_port, my_port;

    DatagramSocket rDS = null;

    SensorManager mSensorManager;
    LocationManager mLocationManager;
    WindowManager windowManager;
    WindowManager.LayoutParams params;
    LinearLayout LL;
    int backTurn = 0;


    Timer mainTimer;					//タイマー用
    MainTimerTask mainTimerTask;		//タイマタスククラス

    Camera camera;

    Boolean isAwake = false;

    float Villumi, VaccelX, VaccelY, VaccelZ, VgyroX, VgyroY, VgyroZ, VorientX, VorientY, VorientZ, VmagX, VmagY, VmagZ, Vproximity;
    double Vlat, Vlng, Valt;
    int Vyear, Vmonth, Vday, Vhour, Vminute, Vsecond, Vwidth, Vheight;

    public UdpService() {
        super("udp service");
    }

    @Override
    public void onCreate() {        // called once at fist
        super.onCreate();

        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensorList) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        LL = new LinearLayout(this);
        LL.setBackgroundColor(Color.argb(0, 0, 0, 255));

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Vwidth = windowManager.getDefaultDisplay().getWidth();
        Vheight = windowManager.getDefaultDisplay().getHeight();

        params = new WindowManager.LayoutParams(    // big blue layer
                Vwidth,
                Vheight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,	//upper screen
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |	//他のアプリと端末ボタンを操作できる
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |	//座標系をスクリーンに合わせる
                        WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |	//WATCH_OUTSIDE_TOUCHと同時利用で
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,	//外側にタッチできる//端末ボタンが無効になる？
                PixelFormat.TRANSLUCENT  				// transparent
        );

        windowManager.addView(LL, params);

        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE); // GPS

        if ( mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Criteria criteria = new Criteria();                                  // Criteriaオブジェクトを生成
            criteria.setAccuracy( Criteria.ACCURACY_MEDIUM );                      // Accuracyを指定(低精度)
            criteria.setPowerRequirement( Criteria.POWER_LOW );                    // PowerRequirementを指定(低消費電力)
            String provider = mLocationManager.getBestProvider( criteria, true );  // ロケーションプロバイダの取得
            mLocationManager.requestLocationUpdates( provider, 0, 0, this );       // LocationListenerを登録
        }

        mainTimer = new Timer();                        //タイマーインスタンス生成
        mainTimerTask = new MainTimerTask();            //タスククラスインスタンス生成
        mainTimer.schedule(mainTimerTask, 0, 500);    //タイマースケジュール設定＆開始 start time , pitch(ms)

        camera = Camera.open(); // for light
        camera.startPreview();


        loadPref();
        isAwake = true;

        receiveOrder(); // start receive thread
    }

    public class MainTimerTask extends TimerTask {  // timer class -> send date
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss", Locale.JAPAN);
            String timeTmp = sdf.format(new Date());
            String[] times = timeTmp.split("/");

            Vyear = Integer.parseInt(times[0]);
            Vmonth = Integer.parseInt(times[1]);
            Vday = Integer.parseInt(times[2]);
            Vhour = Integer.parseInt(times[3]);
            Vminute = Integer.parseInt(times[4]);
            Vsecond = Integer.parseInt(times[5]);

            sendValues("Year:" + Vyear + ",Month:" + Vmonth + ",Day:" + Vday + ",Hour:" + Vhour
                    + ",Minute:" + Vminute + ",Second:" + Vsecond + ",Width:" + Vwidth + ",Height:" + Vheight  + ",", 100);
            //Log.d(TAG, timeTmp);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Vlat = location.getLatitude();
        Vlng = location.getLongitude();
        Valt = location.getAltitude();

        String s = "Lat:" + Vlat + ",Lng:" + Vlng + ",Alt:" + Valt  + ",";

        sendValues(s, 50);
    }
    @Override
    public void onProviderEnabled(String provider) {    }
    @Override
    public void onProviderDisabled(String provider) {    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {      // called every starting

        Toast.makeText(UdpService.this, "onStartCommand", Toast.LENGTH_SHORT).show();


        return START_STICKY;   //明示的にサービスの起動、停止が決められる場合の返り値
    }

    @Override
    protected void onHandleIntent(Intent intent) {  // img action for full activity via broadcast receiver

    }

    public void receiveOrder(){
        new Thread (new Runnable() {

            @Override
            public void run() {

                // http://ameblo.jp/shima-2012/entry-11867688917.html

                try {
                    rDS = new DatagramSocket(my_port);

                    byte[] rByte = new byte[102400];

                    DatagramPacket rDP = new DatagramPacket(rByte, rByte.length);

                    while (isAwake) {
                        rDS.receive(rDP);

                        Log.d("while", "" + rDP.getLength());

                        if (rDP.getLength() < 1000) {

                            String receiveStr = new String(rDP.getData(), 0, rDP.getLength(), Charset.forName("UTF-8"));

                            Log.d(TAG, receiveStr);
                            identifyOrder(receiveStr);

                        } else {        // image received
                            try {
                                Log.d("while", "broad");

                                Intent broadcastIntent = new Intent();

                                broadcastIntent.putExtra("imgArray", rDP.getData());
                                broadcastIntent.putExtra("imgOffset", rDP.getOffset());
                                broadcastIntent.putExtra("imgLength", rDP.getLength());

                                broadcastIntent.setAction("IMG_ACTION");    // filter action

                                getBaseContext().sendBroadcast(broadcastIntent);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    rDS.disconnect();
                    rDS.close();

                } catch( Exception e ) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    public void identifyOrder(String recStr){
        String [] orders = recStr.split(",");

        for (String order: orders){
            if (order.equals("")) continue;

            String[] data = order.split(":");

            switch (data[0]){

                case "toast":
                    Intent ShadowT = new Intent(UdpService.this, ShadowActivity.class);
                    ShadowT.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    ShadowT.putExtra("mode", "toast");
                    ShadowT.putExtra("toastText", data[1]);
                    startActivity(ShadowT);

                    Log.d(TAG, "toast");
                    break;

                case "vibrate":

                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                    vibrator.vibrate(Integer.parseInt(data[1]));

                    Log.d(TAG, "vibrate");
                    break;

                case "background":
                    int R = Integer.parseInt(data[1]);
                    int G = Integer.parseInt(data[2]);
                    int B = Integer.parseInt(data[3]);

                    LL.setBackgroundColor(Color.argb(50, R, G, B));

                    Log.d(TAG, "background backTurn: " + backTurn);

                    break;

                case "image":   // http://tkitao.hatenablog.com/entry/2014/09/21/125829
                    break;

                case "dialog":
                    Intent ShadowD = new Intent(UdpService.this, ShadowActivity.class);
                    ShadowD.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    ShadowD.putExtra("mode", "dialog");
                    ShadowD.putExtra("title", data[1]);
                    ShadowD.putExtra("message", data[2]);
                    ShadowD.putExtra("positive", data[3]);
                    ShadowD.putExtra("negative", data[4]);
                    startActivity(ShadowD);

                    Log.d(TAG, "dialog");
                    break;

                case "sound":
                    Log.d(TAG, "sound");
                    break;

                case "light":
                    Log.d(TAG, "light");

                    Camera.Parameters param = camera.getParameters();

                    if (Integer.parseInt(data[1]) == 1){    // on -> torch
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else {
                        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    camera.setParameters(param);                 // string : camera.getParameters().getFlashMode() -> torch , off

                    break;

                case "site":
                    Log.d(TAG, "site");
                    Intent siteI;
                    if (data.length == 3) siteI = new Intent(Intent.ACTION_VIEW, Uri.parse(data[1] + ":" + data[2]));
                    else siteI = new Intent(Intent.ACTION_VIEW, Uri.parse(data[1]));
                    siteI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    startActivity(siteI);
                    break;

                case "tel":
                    Log.d(TAG, "tel");
                    Intent telI = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + data[1]));
                    telI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    startActivity(telI);
                    break;

                case "mail":
                    Log.d(TAG, "mail");
                    Intent mailI = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + data[1]));
                    mailI.putExtra(Intent.EXTRA_SUBJECT, data[2]);
                    mailI.putExtra(Intent.EXTRA_TEXT, data[3]);

                    mailI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    startActivity(mailI);
                    break;

                case "map":
                    Log.d(TAG, "map");
                    Intent mapI;

                    if (data.length == 4) {
                        mapI = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + data[1] + "," + data[2] + "?q=" + data[3]));        // lat lng keyword
                    } else {
                        mapI = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + data[1] + "," + data[2]));        // lat lng keyword
                    }
                    mapI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);    //start
                    startActivity(mapI);
                    break;


                default:
                    break;
            }
        }

    }

    public void loadPref(){
        try {
            SharedPreferences pref = getSharedPreferences("pref.txt", MODE_PRIVATE);
            remote_IP = pref.getString("remote_IP", "");
            remote_port = Integer.parseInt( pref.getString("remote_port", "") );
            my_IP = pref.getString("my_IP", "");
            my_port = Integer.parseInt( pref.getString("my_port", "") );

        } catch(Exception e){
            Log.e("load file", "cant load");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            case Sensor.TYPE_LIGHT:
                Villumi = event.values[0];
                break;

            case Sensor.TYPE_ACCELEROMETER:
                VaccelX = event.values[0];
                VaccelY = event.values[1];
                VaccelZ = event.values[2];
                break;

            case Sensor.TYPE_GYROSCOPE:
                VgyroX = event.values[0];
                VgyroY = event.values[1];
                VgyroZ = event.values[2];
                sendValues(makeMassage(), 500);
                break;

            case Sensor.TYPE_ORIENTATION:
                VorientX = event.values[0];
                VorientY = event.values[1];
                VorientZ = event.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                VmagX = event.values[0];
                VmagY = event.values[1];
                VmagZ = event.values[2];
                break;

            case Sensor.TYPE_PROXIMITY:
                Vproximity = event.values[0];
                break;

            default:
                break;
        }

    }

    public void sendValues(final String Message, final int len){
        new Thread (new Runnable() {
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

    public String makeMassage(){
        String str = "Illumi:" + Villumi;
        str += ",AccelX:" + VaccelX + ",AccelY:" + VaccelY + ",AccelZ:" + VaccelZ;
        str += ",GyroX:" + VgyroX + ",GyroY:" + VgyroY + ",GyroZ:" + VgyroZ;
        str += ",OrientX:" + VorientX + ",OrientY:" + VorientY + ",OrientZ:" + VorientZ;
        str += ",MagX:" + VmagX + ",MagY:" + VmagY + ",MagZ:" + VmagZ;
        str += ",Proximity:" + Vproximity  + ",";
        return str;
    }

    @Override
    public void onDestroy() {
        Log.d("destroy","close");

        isAwake = false;

        mSensorManager.unregisterListener(this);
        mLocationManager.removeUpdates(this);

        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();


        windowManager.removeView(LL);

        mainTimerTask.cancel();

        Camera.Parameters param = camera.getParameters();

        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        camera.setParameters(param);

        camera.unlock();
        camera.release();
        camera = null;

        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {    }

}

// https://developer.android.com/reference/android/hardware/TriggerEventListener.html
package com.example.leica.udp_3thread;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

// http://pr.cei.uec.ac.jp/kobo2015/index.php?Android%2FTouch
// http://techbooster.jpn.org/andriod/device/3936/ <- double tap
public class MultiTouchView extends View {
    String TAG = "MultiTouchView";

    FullActivity activity;

    ScaleGestureDetector scaleDetector;
    RotateGestureDetector rotateDetector;
    TranslateGestureDetector translateDetector;
    float scale = 100, dScale = 1.0f;
    float rotate = 0, dRotate = 0;
    int translateX = 0, translateY = 0, dTranslateX = 0, dTranslateY = 0;

    Paint paint = new Paint();

    int radius = 100;

    boolean touched;
    int type;
    float pressure;
    int pointer_count;

    int movecount = 0, gestureCount = 0;

    int MAXPOINT = 10;
    int x[] = new int[MAXPOINT];
    int y[] = new int[MAXPOINT];
    int pointer_id[] = new int[MAXPOINT];

    public MultiTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);


        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener  = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {// start scaling
                //Log.d(TAG, "onScaleBegin");
                return super.onScaleBegin(detector);
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {     // scaling ( detector.getScaleFactor = delta scale )
                dScale = detector.getScaleFactor();
                scale *= dScale;
                Log.d(TAG, "onScale : " + scale * 100);
                //activity.sendValues("Scale:" + dScale + ",", 20);
                return true;
            }
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {     // end scaling
                //Log.d(TAG, "onScaleEnd");
                dScale = 1.0f;
                activity.sendValues("Scale:" + 1.0f + ",", 20);
                super.onScaleEnd(detector);
            }
        };

        RotateGestureDetector.SimpleOnRotateGestureListener rotateListener = new RotateGestureDetector.SimpleOnRotateGestureListener() {
            @Override
            public boolean onRotate(float degrees, float focusX, float focusY) {    // degree = delta degree
                dRotate = degrees;
                rotate += dRotate;
                Log.d(TAG, "onRotate : " + rotate);
                return true;
            }
            @Override
            public void onRotateBegin(){
                //Log.d(TAG, "onRotateBegin");
            }
            @Override
            public void onRotateEnd(){
                //Log.d(TAG, "onRotateEnd");
                dRotate = 0;
                activity.sendValues("Rotate:" + 0 + ",", 20);
            }
        };

        TranslateGestureDetector.SimpleOnTranslateGestureListener translateListener = new TranslateGestureDetector.SimpleOnTranslateGestureListener() {
            @Override
            public boolean onTranslate(int difX, int difY) {    // degree = delta degree
                dTranslateX = difX;
                dTranslateY = difY;

                translateX += difX;
                translateY += difY;

                Log.d(TAG, "onTranslate x : " + translateX + " , y : " + translateY);
                return true;
            }
            @Override
            public void onTranslateBegin(){
                //Log.d(TAG, "onTranslateBegin");
            }
            @Override
            public void onTranslateEnd(){
                //Log.d(TAG, "onTranslateEnd");
                dTranslateX = 0;
                dTranslateY = 0;

                activity.sendValues("TranslateX:" + dTranslateX + ",TranslateY:" + dTranslateY + ",", 30);
            }
        };

        scaleDetector = new ScaleGestureDetector(context, scaleListener);
        rotateDetector = new RotateGestureDetector(context, rotateListener);
        translateDetector = new TranslateGestureDetector(context, translateListener);

        for (int i = 0; i < MAXPOINT; i++) {
            x[i] = -999;
            y[i] = -999;
        }

        setBackgroundColor(Color.TRANSPARENT);
        paint.setTextSize(40);
    }

    public void setFullActivity(FullActivity act){
        activity = act;
    }

    @Override
    protected void onDraw(Canvas canvas) {  // = onResume loop
        super.onDraw(canvas);

        setBackgroundColor(Color.TRANSPARENT);

        if (touched) {
            for (int i = 0; i < pointer_count; i++) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setARGB(255, 255, 255, 255);  // white line

                paint.setAntiAlias(true);   // smooth

                canvas.drawCircle(x[i], y[i], radius, paint);

                String s = "Order: " + i + ", ID: " + pointer_id[i] + ", X: " + x[i] + ", Y: " + y[i];
                canvas.drawText(s, x[i] - 300, y[i] - 2 * radius, paint);
            }
            touched = false;
        }

        //invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        scaleDetector.onTouchEvent(event);
        rotateDetector.onTouchEvent(event);
        translateDetector.onTouchEvent(event);

        if (event.getPointerCount() == 2 && event.getActionMasked() == MotionEvent.ACTION_MOVE){
            gestureCount++;

            if (gestureCount %3 == 0) {
                activity.sendValues("Scale:" + dScale + "," + ",Rotate:" + dRotate + ",TranslateX:" + dTranslateX + ",TranslateY:" + dTranslateY + ",", 100);
            }
        }

        type = event.getActionMasked();
        pressure = event.getPressure();
        pointer_count = event.getPointerCount();

        touched = true;

        if (pointer_count == 1 && type == MotionEvent.ACTION_UP){ // all fingers released
            touched = false;
            pointer_count = 0;
            pressure = 0;
        }

        for (int i = 0; i < MAXPOINT; i++) {

            if (i < pointer_count) {
                x[i] = (int) event.getX(i);
                y[i] = (int) event.getY(i);
                pointer_id[i] = event.getPointerId(i);
            } else {
                x[i] = -999;
                y[i] = -999;
            }
        }

        // send process here   TouchPoint:2,TouchX:50_100_-999,TouchY:20_40_-999,

        String s = "TouchPoint:" + pointer_count + ",TouchX:" + x[0];

        for (int i = 1; i < MAXPOINT; i++) s += "_" + x[i];

        s += ",TouchY:" + y[0];

        for (int i = 1; i < MAXPOINT; i++) s += "_" + y[i];

        s += ",";

        //tv.setText(s);

        if (type == MotionEvent.ACTION_MOVE) {  // reduce move
            movecount++;

            if (movecount % 3 == 0) activity.sendValues(s, 150);   // remote control Full activity
        }
        else activity.sendValues(s, 150);   // remote control Full activity


        invalidate();   // refresh view = onDraw()

        return true;    // do not transmit touch event
    }

}

// http://qiita.com/niusounds/items/bee2758d828b1e09c630
class RotateGestureDetector {   // focusXY is the center on two fingers

    public interface OnRotateListener {
        boolean onRotate(float degrees, float focusX, float focusY);

        void onRotateBegin();

        void onRotateEnd();
    }

    public static class SimpleOnRotateGestureListener implements OnRotateListener {
        @Override
        public boolean onRotate(float degrees, float focusX, float focusY) {
            return false;
        }
        @Override
        public void onRotateBegin(){    }
        @Override
        public void onRotateEnd(){    }
    }

    static float RADIAN_TO_DEGREES = (float) (180.0 / Math.PI);
    private OnRotateListener listener;
    private float prevX = 0.0f;
    private float prevY = 0.0f;

    Context con;
    float tan = 0.0f;
    float prevTan = 0.0f;

    public RotateGestureDetector(Context _con, OnRotateListener listener) {
        this.con = _con;
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 2){
            prevX = prevY = 0.0f;
            tan = prevTan = 0.0f;
            listener.onRotateEnd();
        }

        if (event.getPointerCount() == 2 && event.getActionMasked() == MotionEvent.ACTION_MOVE) {   // rotating
            boolean result = true;
            float x = event.getX(1) - event.getX(0);
            float y = event.getY(1) - event.getY(0);
            float focusX = (event.getX(1) + event.getX(0)) * 0.5f;
            float focusY = (event.getY(1) + event.getY(0)) * 0.5f;
            float tan = (float) Math.atan2(y, x);

            if (prevX == 0.0f && prevY == 0.0f) {   // case : first move
                prevTan = tan;
                listener.onRotateBegin();
            } else {
                float degree = (tan - prevTan) * RADIAN_TO_DEGREES;

                if (degree > 180) degree -= 360;        // for tan jump
                if (degree < -180) degree += 360;

                result = listener.onRotate(degree, focusX, focusY);
            }
            prevX = x;
            prevY = y;
            prevTan = tan;

            return result;

        } else {
            return true;
        }
    }
}

// cf RotateGestureDetector
class TranslateGestureDetector {

    public interface OnTranslateListener {
        boolean onTranslate(int defX, int defY);

        void onTranslateBegin();

        void onTranslateEnd();
    }

    public static class SimpleOnTranslateGestureListener implements OnTranslateListener {   // value from center of two fingers
        @Override
        public boolean onTranslate(int difX, int difY) {
            return false;
        }
        @Override
        public void onTranslateBegin(){    }
        @Override
        public void onTranslateEnd(){    }
    }

    private OnTranslateListener listener;

    private int prevX = 0, prevY = 0;

    Context con;

    public TranslateGestureDetector(Context _con, OnTranslateListener listener) {
        this.con = _con;
        this.listener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 2){  // end
            prevX = prevY = 0;
            listener.onTranslateEnd();
        }

        if (event.getPointerCount() == 2 && event.getActionMasked() == MotionEvent.ACTION_MOVE) {   // rotating
            boolean result = true;

            int cx = (int)((event.getX(1) + event.getX(0)) * 0.5);
            int cy = (int)((event.getY(1) + event.getY(0)) * 0.5f);

            if (prevX == 0.0f && prevY == 0.0f) {   // case : first move
                prevX = cx;
                prevY = cy;
                listener.onTranslateBegin();
            } else {                                //moving
                int difX = cx - prevX;
                int difY = cy - prevY;

                result = listener.onTranslate(difX, difY);
            }
            prevX = cx;
            prevY = cy;

            return result;

        } else {
            return true;
        }
    }
}
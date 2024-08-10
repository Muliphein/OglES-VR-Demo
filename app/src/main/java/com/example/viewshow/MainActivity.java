package com.example.viewshow;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    ObjectLightRender renderer;

    private SensorManager DirSensorManager;
    private Sensor DirSensor;
    private SensorEventListener myDirSensorListener;

    private SensorManager Accersensormanager;
    private Sensor AccerSensor;
    private SensorEventListener myAccerSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        initView();

        renderer.setFocusLength(FocusLength);
        renderer.setLightStrength(1.5f);
        renderer.setBias(bias);
        renderer.setScale(0.3f, 0.3f, 0.3f , 0);
        renderer.setDelta(0f, 1f, 0f, 0);
        renderer.setScale(1f, 1f, 1f, 1);
        renderer.setDelta(0f, 0f, 0f, 1);
        renderer.setScale(0.5f, 0.5f, 0.5f, 2);
        renderer.setDelta(10f, 10f, 10f, 2);
        renderer.setScale(0.2f, 1.5f, 0.2f, 3);
        renderer.setDelta(-10f, 5f, 20f, 3);
        renderer.setScale(1.5f, 0.5f, 1.5f, 4);
        renderer.setDelta(10f, 0f, 15f, 4);

        renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);
        renderer.SetLightPosition((lightPosX%Upper_Bound_X)/Upper_Bound_X*2.0f*(float)Math.PI
                ,(float)Math.PI / 4f
                ,lightDistance);


        TimerTask task= new TimerTask() {
            @Override
            public void run() {
                lightPosX += 0.5f;
                renderer.SetLightPosition(
                        (lightPosX%Upper_Bound_X)/Upper_Bound_X*2.0f*(float)Math.PI
                        ,(float)Math.PI / 4f
                        ,lightDistance);
            }
        };

        Timer time = new Timer("TurnAround");
        time.schedule(task, 0, 20);


        Accersensormanager = (SensorManager) this.getSystemService(Service.SENSOR_SERVICE);
        AccerSensor = Accersensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myAccerSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
//                viewVertical = event.values[2]/9.9f * ((float)Math.PI) / 2.0f;
//                renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };


        /*
        Accersensormanager.registerListener(myAccerSensorListener, AccerSensor, SensorManager.SENSOR_DELAY_GAME);




        //获取SensorManager实例
        DirSensorManager = (SensorManager) this.getSystemService(Service.SENSOR_SERVICE);
        //获取Sensor实例
        DirSensor = DirSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        myDirSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.v("Sensor", "0: " + event.values[0] + " 1: " + event.values[1] +" 2: " + event.values[2]);
                viewHorizon = event.values[0]/360.0f * ((float)Math.PI) * 2.0f;
                viewVertical = event.values[1]/180.0f * (((float)Math.PI) /2.0f - 1e-2f);
                Log.v("View", "H: "+viewHorizon + "; V: "+viewVertical);
                viewHorizon = 0;
                renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        //注册滚动事件
        DirSensorManager.registerListener(myDirSensorListener, DirSensor, SensorManager.SENSOR_DELAY_UI);
        */




    }

    ObjectInfo resources[] = new ObjectInfo[10];

    public float[] downPosition = new float[2];

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPosition[0] = event.getX();
                downPosition[1] = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:

                viewVertical -= (y - downPosition[1])/1024.0f*0.02f;
                viewHorizon +=  (x - downPosition[0])/1024.0f*0.02f;
                renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);
                break;
        }

        return true;
    }

    private void initView() {
        glSurfaceView = findViewById(R.id.glsurfaceview);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new ObjectLightRender(this );

        //Project ObjectFile with Light Render
        resources[0] = ObjGetIn.GetObj(this,"SuqareCube.obj");
        resources[1] = ObjGetIn.GetObj(this, "Teapot.obj");
        resources[2] = ObjGetIn.GetObj(this, "midCube.obj");

        float [] tempPointArray = Obj2Array.PointToArray(resources[1]);
        float [] tempNormalArray = Obj2Array.NormalToArray(resources[1]);
        float [] tempTexArray = Obj2Array.TextToArray(resources[1]);
        renderer.addObject(tempPointArray, tempNormalArray, tempTexArray, R.drawable.texture0); // 0 Teqpot

        tempPointArray = Obj2Array.PointToArray(resources[2]);
        tempNormalArray = Obj2Array.NormalToArray(resources[2]);
        tempTexArray = Obj2Array.TextToArray(resources[2]);
        renderer.addObject(tempPointArray, tempNormalArray, tempTexArray, R.drawable.texture1); // 1 Table

        tempPointArray = Obj2Array.PointToArray(resources[0]);
        tempNormalArray = Obj2Array.NormalToArray(resources[0]);
        tempTexArray = Obj2Array.TextToArray(resources[0]);
        renderer.addObject(tempPointArray, tempNormalArray, tempTexArray, R.drawable.texture2); //2 Cube fly
        renderer.addObject(tempPointArray, tempNormalArray, tempTexArray, R.drawable.texture3); //3 Cube vertical
        renderer.addObject(tempPointArray, tempNormalArray, tempTexArray, R.drawable.texture4); //4 Cube lie

        glSurfaceView.setRenderer(renderer);

    }

    static final int Upper_Bound_X = 1000;// (-Upper_Bound_X, Upper_Bound_X)

    public float lightPosX = 0;
    public float lightDistance = 50f;
    public float viewX = 0;
    public float viewY = 10f;
    public float viewZ = 50f;
    public float bias = 0f;
    public float viewHorizon = 180.4f;
    public float FocusLength = 20f;
    public float viewVertical = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.v("Key Down", " "+keyCode + " " + event.getSource());
        int Source = event.getSource();
        if ((Source & InputDevice.SOURCE_JOYSTICK)
                == InputDevice.SOURCE_JOYSTICK) {
            switch (keyCode){
                case 19: viewVertical += 0.02f; break;
                case 20: viewVertical -= 0.02f; break;
                case 21: viewHorizon -= 0.05f; break;
                case 22: viewHorizon += 0.05f; break;
            }
            viewVertical = Math.min(viewVertical, (float)Math.PI /2.0f -1e-3f);
            viewVertical = Math.max(viewVertical, - (float)Math.PI /2.0f +1e-3f);
            renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);

        } else if ((Source & InputDevice.SOURCE_GAMEPAD)
                        == InputDevice.SOURCE_GAMEPAD){
//            Log.v("GAMEPAD", " "+ keyCode);
            switch (keyCode){
                case 103: //RB
                    viewX += 1f * Math.cos(viewHorizon);
                    viewZ += 1f * Math.sin(viewHorizon);
                    break;
                case 102: //LB
                    viewX -= 1f * Math.cos(viewHorizon);
                    viewZ -= 1f * Math.sin(viewHorizon);
                    break;
                case 100: //Y
                    FocusLength += 0.1f;
                    break;
                case 96: //A
                    FocusLength -= 0.1f;
                    break;
                case 97: //B
                    bias += 0.005f;
                    break;
                case 99: //X
                    bias -= 0.005f;
                    break;
                default:
                    Log.v("GAMEPAD", " "+ keyCode);
            }
            FocusLength = Math.max(FocusLength, 10f);
            viewX = Math.max(viewX, -59f);
            viewX = Math.min(viewX, 59f);
            viewZ = Math.max(viewZ, -59f);
            viewZ = Math.min(viewZ, 59f);
            bias = Math.min(bias, 0.20f);
            bias = Math.max(bias, -0.20f);
            renderer.setBias(bias);
            renderer.setFocusLength(FocusLength);
            renderer.SetViewPosition(viewX, viewY, viewZ, viewHorizon, viewVertical);
        }
        return true;
    }

}

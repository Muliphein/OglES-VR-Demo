package com.example.viewshow;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.FaceDetector;
import android.nfc.Tag;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResReadUtils {

    static float [] VertexArray;
    static float [] ColorArray;
    static short [] IndicesArray;

    /*
        读取Shader文件
     */
    public static String readResource(Context mycontext, String fileName){
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = mycontext.getAssets().open(fileName);
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                builder.append(textLine);
                builder.append("\n");
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static void readObjResource(Context mycontext, String fileName){
//        Log.v("Readin","Start");
        int PointNumber;
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = mycontext.getAssets().open(fileName);
            InputStreamReader streamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                builder.append(textLine);
                builder.append(" ");
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
        String Input = builder.toString();
//        Log.v("Readin",Input);
        String[] Value = Input.split(" ");
        PointNumber = Integer.parseInt(Value[0]);
//        Log.v("PointNumber", String.valueOf(PointNumber));

//        for (int i=0; i<Value.length; ++i){
//            Log.v("ShowPoint", "|"+Value[i]+"|");
//        }

        float [] Vertex = new float[PointNumber*3];
        for (int i = 0 ; i <PointNumber ; ++i ) {
            for (int j=0; j<3; ++j){
                Vertex[i*3+j] = Float.parseFloat(Value[1+i*3+j]);
//                Log.v("ShowPoint"+String.valueOf(i)+","+String.valueOf(j), "|"+Value[1+i*3+j]+"|");
            }
        }
//        Log.v("Oh ","ShowOver");

        float [] Color = new float[PointNumber*4];
        for (int i=0; i < PointNumber; ++i){
            for (int j=0; j<4; ++j){
                Color[i*4+j] = Float.parseFloat(Value[1+3*PointNumber+i*4+j]);
            }
        }

//        Log.v("Oh ","Color Over");
//        Log.v("FaceNumber",Value[1+7*PointNumber]);
        int FaceNumber=Integer.parseInt(Value[1+7*PointNumber]);
//        Log.v("FaceNumber",String.valueOf(FaceNumber));
        short [] Indices = new short [FaceNumber*3];
        for (int i=0; i < FaceNumber; ++i){
            for (int j=0; j<3;++j){
                Indices[i*3+j] = Short.parseShort(Value[2+7*PointNumber+i*3+j]);
            }
        }
        VertexArray = Vertex;
        ColorArray = Color;
        IndicesArray = Indices;
        return ;
    }

    public static float[] getVertex(){
        return VertexArray;
    }

    public static float[] getColor(){
        return ColorArray;
    }

    public static short[] getIndices(){
        return IndicesArray;
    }
}

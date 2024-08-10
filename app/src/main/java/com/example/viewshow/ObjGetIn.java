package com.example.viewshow;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ObjGetIn {
    static public ObjectInfo GetObj(Context mycontext, String fileName){
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = mycontext.getAssets().open(fileName);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
//                if (textLine.contains("#")) continue;
                builder.append(textLine);
                builder.append(" ");
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
//        Log.v("TAG","String read over");
        ObjectInfo ObjRes = new ObjectInfo();
        String Input = builder.toString();
        String[] Value = Input.split(" |/");
//        Log.v("Show", "Value[1] = "+Value[1]);
        int p = 0;
//        Log.v("Value Length",String.valueOf(Value.length));

        int cnt1=0,cnt2=0,cnt3=0,cnt4=0;
        while (p<Value.length){
//            Log.v("Show", "p = "+ p + "; Value = "+Value[p]);
            while (Value[p].charAt(0)!='v' && Value[p].charAt(0)!='f')
            {
//                Log.v("Show Skip String ",Value[p]);
                p++;
            }
//            Log.v("Position"," p = "+String.valueOf(p));
            if (Value[p].equals("v")){
                p++;
                float[] temp = new float[3];
                for (int i=0; i<3; ++i) {
                    while (Value[p].length() == 0) p++;
                    temp[i]=Float.parseFloat(Value[p]); p++;
                }
                Vec3 tempVec3 = new Vec3(temp[0], temp[1], temp[2]);
//                if (cnt1<=10){
//                    Log.v("Add Point : ", tempVec3.toString());
//                    cnt1++;
//                }

                ObjRes.PointArray.add(new Vec3(temp[0], temp[1], temp[2]));
            } else if (Value[p].equals("vt")){
                p++;
                float[] temp = new float[3];
                for (int i=0; i<3; ++i) {
                    while (Value[p].length() == 0) p++;
                    temp[i]=Float.parseFloat(Value[p]); p++;
                }

                Vec3 tempVec3 = new Vec3(temp[0], temp[1], temp[2]);
//                if (cnt2<=10){
//                    Log.v("Add Texture Point : ", tempVec3.toString());
//                    cnt2++;
//                }

                ObjRes.PointTextureArray.add(new Vec3(temp[0], temp[1], temp[2]));
            } else if (Value[p].equals("vn")){
                p++;
                float[] temp = new float[3];
                for (int i=0; i<3; ++i) {
                    while (Value[p].length() == 0) p++;
                    temp[i]=Float.parseFloat(Value[p]); p++;
                }

                Vec3 tempVec3 = new Vec3(temp[0], temp[1], temp[2]);
//                if (cnt3<=10){
//                    Log.v("Add Normal Point : ", tempVec3.toString());
//                    cnt3++;
//                }


                ObjRes.PointNormalArray.add(new Vec3(temp[0], temp[1], temp[2]));
            } else if (Value[p].equals("f")){
                p++;
                int[] temp = new int[9];
                for (int i=0; i<9; ++i){
                    while (Value[p].length() == 0) p++;
                    temp[i]=Integer.parseInt(Value[p]); p++;
                }

                FaceInfo tempFace = new FaceInfo(new Int3(temp[0], temp[1], temp[2]), new Int3(temp[3], temp[4], temp[5]), new Int3(temp[6], temp[7], temp[8]));
//                if (cnt4<=10){
//                    Log.v("Add Face Info : ",tempFace.toString());
//                    cnt4++;
//                }


                ObjRes.FaceArray.add(new FaceInfo(new Int3(temp[0], temp[1], temp[2]), new Int3(temp[3], temp[4], temp[5]), new Int3(temp[6], temp[7], temp[8])));
            } else {
//                Log.v("Error ", Value[p]);
                p++;
            }
        }
        return ObjRes;
    }
}

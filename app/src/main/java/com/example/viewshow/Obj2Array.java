package com.example.viewshow;

import android.content.Context;

public class Obj2Array {
    static public float[] objToArray(ObjectInfo myObj){
        int length = myObj.FaceArray.size();
        float[] res = new float[length*18];
        for (int i=0; i<length; ++i){
            int[] Point = new int[3];
            int[] Normal = new int[3];
            Point[0] = myObj.FaceArray.get(i).v1.x-1;
            Point[1] = myObj.FaceArray.get(i).v2.x-1;
            Point[2] = myObj.FaceArray.get(i).v3.x-1;

            Normal[0] = myObj.FaceArray.get(i).v1.z-1;
            Normal[1] = myObj.FaceArray.get(i).v2.z-1;
            Normal[2] = myObj.FaceArray.get(i).v3.z-1;

            for (int j=0; j<3; ++j){
                res[i*18 + j*6    ] = myObj.PointArray.get(Point[j]).x;
                res[i*18 + j*6 + 1] = myObj.PointArray.get(Point[j]).y;
                res[i*18 + j*6 + 2] = myObj.PointArray.get(Point[j]).z;

                res[i*18 + j*6 + 3] = myObj.PointNormalArray.get(Normal[j]).x;
                res[i*18 + j*6 + 4] = myObj.PointNormalArray.get(Normal[j]).y;
                res[i*18 + j*6 + 5] = myObj.PointNormalArray.get(Normal[j]).z;
            }

        }
        return res;
    }
    static public float[] PointToArray(ObjectInfo myObj){
        int length = myObj.FaceArray.size();
        float[] res = new float[length*18];
        for (int i=0; i<length; ++i){
            int[] Point = new int[3];
            Point[0] = myObj.FaceArray.get(i).v1.x-1;
            Point[1] = myObj.FaceArray.get(i).v2.x-1;
            Point[2] = myObj.FaceArray.get(i).v3.x-1;

            for (int j=0; j<3; ++j){
                res[i*9 + j*3    ] = myObj.PointArray.get(Point[j]).x;
                res[i*9 + j*3 + 1] = myObj.PointArray.get(Point[j]).y;
                res[i*9 + j*3 + 2] = myObj.PointArray.get(Point[j]).z;
            }

        }
        return res;
    }

    static public float[] NormalToArray(ObjectInfo myObj){
        int length = myObj.FaceArray.size();
        float[] res = new float[length*18];
        for (int i=0; i<length; ++i){
            int[] Point = new int[3];
            Point[0] = myObj.FaceArray.get(i).v1.z-1;
            Point[1] = myObj.FaceArray.get(i).v2.z-1;
            Point[2] = myObj.FaceArray.get(i).v3.z-1;

            for (int j=0; j<3; ++j){
                res[i*9 + j*3    ] = myObj.PointNormalArray.get(Point[j]).x;
                res[i*9 + j*3 + 1] = myObj.PointNormalArray.get(Point[j]).y;
                res[i*9 + j*3 + 2] = myObj.PointNormalArray.get(Point[j]).z;
            }

        }
        return res;
    }

    static public float[] TextToArray(ObjectInfo myObj){
        int length = myObj.FaceArray.size();
        float[] res = new float[length*6];
        for (int i=0; i<length; ++i){
            int[] Point = new int[3];
            Point[0] = myObj.FaceArray.get(i).v1.y-1;
            Point[1] = myObj.FaceArray.get(i).v2.y-1;
            Point[2] = myObj.FaceArray.get(i).v3.y-1;

            for (int j=0; j<3; ++j){
                res[i*6 + j*2    ] = myObj.PointTextureArray.get(Point[j]).x;
                res[i*6 + j*2 + 1] = myObj.PointTextureArray.get(Point[j]).y;
            }

        }
        return res;
    }
}

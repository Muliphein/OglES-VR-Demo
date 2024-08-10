package com.example.viewshow;

import java.util.ArrayList;

public class ObjectInfo {
    ArrayList<Vec3> PointArray;
    ArrayList<Vec3> PointNormalArray;
    ArrayList<Vec3> PointTextureArray;
    ArrayList<FaceInfo> FaceArray;
    //v1 v2 v3
    //v vt vn
    ObjectInfo(){
        PointArray = new ArrayList<Vec3>();
        PointNormalArray = new ArrayList<Vec3>();
        PointTextureArray = new ArrayList<Vec3>();
        FaceArray = new ArrayList<FaceInfo>();
    }
}

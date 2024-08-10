package com.example.viewshow;

public class FaceInfo {
    public Int3 v1,v2,v3;
    public FaceInfo(Int3 vv1, Int3 vv2,Int3 vv3){
        v1 = vv1;
        v2 = vv2;
        v3 = vv3;
    }
    @Override
    public String toString() {
        return v1.toString() + "|" + v2.toString() + "|" + v3.toString();
    }
}

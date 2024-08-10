package com.example.viewshow;

public class Vec3 {
    public float x,y,z;
    public Vec3(float xx, float yy, float zz){
        x = xx;
        y = yy;
        z = zz;
    }

    @Override
    public String toString() {
        return String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);
    }
}

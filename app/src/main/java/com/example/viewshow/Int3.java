package com.example.viewshow;

public class Int3 {
    public int x, y, z;
    public Int3(int xx, int yy, int zz){
        x = xx;
        y = yy;
        z = zz;
    }

    @Override
    public String toString() {
        return String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);
    }

}

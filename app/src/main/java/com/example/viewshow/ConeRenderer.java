package com.example.viewshow;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ConeRenderer implements GLSurfaceView.Renderer {
    private static final int BYTES_PER_FLOAT = 4;
    //顶点位置缓存
    private final FloatBuffer vertexBuffer;
    //顶点颜色缓存
    private final FloatBuffer colorBuffer;
    //渲染程序
    private int mProgram;

    //相机矩阵
    private final float[] mViewMatrix = new float[16];
    //投影矩阵
    private final float[] mProjectMatrix = new float[16];
    //最终变换矩阵
    private final float[] mMVPMatrix = new float[16];

    //返回属性变量的位置
    //变换矩阵
    private int uMatrixLocation;
    //位置
    private int aPositionLocation;
    //颜色
    private int aColorLocation;

    //圆形顶点位置
    private float circularCoords[];
    //顶点的颜色
    private float color[];


    private float x;
    private float y;
    private float distance;


    String vertexShaderStr;
    String fragmentShaderStr;

    public ConeRenderer(String Vertex, String Fragment) {
        distance = 6.0f;
        x = 0f;
        y = 0f;
        vertexShaderStr = Vertex;
        fragmentShaderStr = Fragment;
        createPositions(0.5f,60);

        //顶点位置相关
        //分配本地内存空间,每个浮点型占4字节空间；将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = ByteBuffer.allocateDirect(circularCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(circularCoords);
        vertexBuffer.position(0);

        //顶点颜色相关
        colorBuffer = ByteBuffer.allocateDirect(color.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);
    }


    public void SetViewPosition(float xx, float yy, float ddistance){
        x = xx;
        y = yy;
        distance = ddistance;
        //设置相机位置
//        Log.v("Now Position ",
//                " x : "+String.valueOf((float)(distance*Math.cos(y)*Math.sin(x)))+
//                " y : "+String.valueOf((float)(distance*Math.sin(y)))+
//                " z : "+(float)(distance*Math.cos(y)*Math.cos(x)));
        Matrix.setLookAtM(
                mViewMatrix, 0,
                (float)(distance*Math.cos(y)*Math.sin(x)), (float)(distance*Math.sin(y)), (float)(distance*Math.cos(y)*Math.cos(x)),
                0f, 0f, 0f, 0f, 1.0f, 0.0f
        );
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }


    private void createPositions(float radius, int n){
        ArrayList<Float> data=new ArrayList<>();
        data.add(0.0f);             //设置圆锥顶点坐标
        data.add(0.0f);
        data.add(-0.5f);
        float angDegSpan=360f/n;
        for(float i=0;i<360+angDegSpan;i+=angDegSpan){
            data.add((float) (radius*Math.sin(i*Math.PI/180f)));
            data.add((float)(radius*Math.cos(i*Math.PI/180f)));
            data.add(0.0f);
        }
        float[] f=new float[data.size()];
        for (int i=0;i<f.length;i++){
            f[i]=data.get(i);
        }

        circularCoords = f;

        //处理各个顶点的颜色
        color = new float[f.length*4/3];
        ArrayList<Float> tempC = new ArrayList<>();
        ArrayList<Float> totalC = new ArrayList<>();
        ArrayList<Float> total0 = new ArrayList<>();
        total0.add(0.5f);
        total0.add(0.0f);
        total0.add(0.0f);
        total0.add(1.0f);
        tempC.add(1.0f);
        tempC.add(1.0f);
        tempC.add(1.0f);
        tempC.add(1.0f);
        for (int i=0;i<f.length/3;i++){
            if (i==0){
                totalC.addAll(total0);
            }else {
                totalC.addAll(tempC);
            }

        }

        for (int i=0; i<totalC.size();i++){
            color[i]=totalC.get(i);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为白色
        GLES20.glClearColor(0.5f,0.5f,0.5f,1.0f);

        //编译顶点着色程序
//        String vertexShaderStr = ResReadUtils.readResource(R.raw.vertex_simple_shade);
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
//        String fragmentShaderStr = ResReadUtils.readResource(R.raw.fragment_simple_shade);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);
        //在OpenGLES环境中使用程序
        GLES20.glUseProgram(mProgram);


        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_Matrix");
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "vPosition");
        aColorLocation = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘制窗口
        GLES20.glViewport(0, 0, width, height);


        //相机和透视投影方式
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 6, 0, -1f, 0f, 0f, 0f, 0f, 0.0f, 1.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    float temp = 0;
    float temp2 = 0;
    int flag = 0;

    @Override
    public void onDrawFrame(GL10 gl) {


        temp += 0.003f;
//        if (flag == 0) temp2 += 0.002f;
//        else temp2 -= 0.002f;
        if (temp >= 2* Math.PI) temp -= 2*Math.PI;
//        if (temp2 >= Math.PI/2.0f) flag = 1;
//        if (temp2 <= -Math.PI/2.0f) flag = 0;
        SetViewPosition(temp,temp2 ,distance);

        //把颜色缓冲区设置为我们预设的颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,mMVPMatrix,0);
        //准备坐标数据
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //启用顶点位置句柄
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        //准备颜色数据
        GLES20.glVertexAttribPointer(aColorLocation, 4, GLES20.GL_FLOAT, false, 0, colorBuffer);
        //启用顶点颜色句柄
        GLES20.glEnableVertexAttribArray(aColorLocation);

        //绘制圆锥侧面
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, circularCoords.length/3);

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
    }
}

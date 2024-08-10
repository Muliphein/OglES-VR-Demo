package com.example.viewshow;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ObjectRender implements GLSurfaceView.Renderer {

    private final String vertexShaderStr;
    private final String fragmentShaderStr;

    //一个Float占用4Byte
    private static final int BYTES_PER_FLOAT = 4;
    //三个顶点
//    private static final int POSITION_COMPONENT_COUNT = 4;
    //顶点位置缓存
    private final FloatBuffer vertexBuffer;
    //顶点颜色缓存
    private final FloatBuffer colorBuffer;
    //索引缓存
    private final ShortBuffer indicesBuffer;

    //渲染程序
    private int mProgram;

    private float ShapeCoords[] ={
            0.5f, 0.5f, 0.5f
            ,0.5f ,0.5f ,-0.5f
            ,0.5f ,-0.5f ,-0.5f
            ,-0.5f ,0.5f ,-0.5f
    };

    //Coords Index
    private short[] indices ={
            0 ,2 ,1
            ,0 ,1 ,3
            ,0 ,3 ,2
            ,1 ,2 ,3
    };
    //四个顶点的颜色参数
    private float color[]={
            1.0f ,0.0f ,0.0f ,1.0f
            ,0.0f ,1.0f ,0.0f ,1.0f
            ,0.0f ,0.0f ,1.0f ,1.0f
            ,0.5f ,0.5f ,0.5f ,1.0f
    };

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

    private float viewX;
    private float viewY;
    private float viewDistance;

    public ObjectRender(String VShader, String FShader,float [] VertexArray, float[] ColorArray, short[] IndicesArray) {
        vertexShaderStr = VShader;
        fragmentShaderStr = FShader;
        color = ColorArray;
        ShapeCoords = VertexArray;
        indices = IndicesArray;
        //顶点位置相关
        //分配本地内存空间,每个浮点型占4字节空间；将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        vertexBuffer = ByteBuffer.allocateDirect(ShapeCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(ShapeCoords);
        vertexBuffer.position(0);

        //顶点颜色相关
        colorBuffer = ByteBuffer.allocateDirect(color.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        colorBuffer.put(color);
        colorBuffer.position(0);

        //顶点索引相关
        indicesBuffer = ByteBuffer.allocateDirect(indices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        indicesBuffer.put(indices);
        indicesBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //将背景设置为白色
        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST); // 开启深度测试
        GLES20.glDepthFunc(GLES20.GL_LESS); //基准设置为 1.0，那么GL_LESS 则深度小余 1.0 的通过测试

        //编译顶点着色程序
        int vertexShaderId = ShaderUtils.compileVertexShader(vertexShaderStr);
        //编译片段着色程序
        int fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShaderStr);
        //连接程序
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);

        uMatrixLocation = GLES20.glGetUniformLocation(mProgram, "u_Matrix");
        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "vPosition");
        aColorLocation = GLES20.glGetAttribLocation(mProgram, "aColor");

        //在OpenGLES环境中使用程序
        GLES20.glUseProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置绘制窗口
        GLES20.glViewport(0, 0, width, height);
        //计算宽高比
        float ratio=(float)width/height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
        //设置相机位置
        Matrix.setLookAtM(
                mViewMatrix, 0,
                (float)(viewDistance*Math.cos(viewY)*Math.sin(viewX)), (float)(viewDistance*Math.sin(viewY)), (float)(viewDistance*Math.cos(viewY)*Math.cos(viewX)),
                0f, 0f, 0f, 0f, 1.0f, 0.0f
        );
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    public void SetViewPosition(float xx, float yy, float ddistance){
        viewX = xx;
        viewY = yy;
        viewDistance = ddistance;
        //设置相机位置
//        Log.v("Now Position ",
//                " x : "+String.valueOf((float)(distance*Math.cos(y)*Math.sin(x)))+
//                " y : "+String.valueOf((float)(distance*Math.sin(y)))+
//                " z : "+(float)(distance*Math.cos(y)*Math.cos(x)));
        Matrix.setLookAtM(
                mViewMatrix, 0,
                (float)(viewDistance*Math.cos(viewY)*Math.sin(viewX)), (float)(viewDistance*Math.sin(viewY)), (float)(viewDistance*Math.cos(viewY)*Math.cos(viewX)),
                0f, 0f, 0f, 0f, 1.0f, 0.0f
        );
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //把颜色缓冲区设置为我们预设的颜色,并且清空缓存
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT |  GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);
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

//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12);
        GLES20.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_SHORT, indicesBuffer);

        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aColorLocation);
    }

    public void setViewD(float ddistance){
        Log.v("Set X : ",String.valueOf(ddistance));
        viewDistance = ddistance;
        SetViewPosition(viewX,viewY,viewDistance);
    }

    public void setViewX(float xx){
        Log.v("Set X : ",String.valueOf(xx));
        viewX = xx;
        SetViewPosition(viewX,viewY,viewDistance);
    }

    public void setViewY(float yy){
        Log.v("Set Y : ",String.valueOf(yy));
        viewY = yy;
        SetViewPosition(viewX,viewY,viewDistance);
    }

}

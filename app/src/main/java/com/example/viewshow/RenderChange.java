package com.example.viewshow;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderChange implements GLSurfaceView.Renderer {

    private final String vertexShaderStr;
    private final String fragmentShaderStr;

    //一个Float占用4Byte
    private static final int BYTES_PER_FLOAT = 4;

    private int MAX_OBJ = 8;
    private int MAX_OBJ_TEST = 10000;

    //顶点位置缓存
    private FloatBuffer[] vertexBuffer = new FloatBuffer[MAX_OBJ];

    //法向量缓存
    private FloatBuffer[] normalBuffer = new FloatBuffer[MAX_OBJ];

    //渲染程序
    private int mProgram;

    private float[][] ShapeCoords = new float[MAX_OBJ_TEST][];

    private float[][] NormalCoords = new float[MAX_OBJ_TEST][];

    private float LightPosition[]={0f, 0f, 0f};

    private float LightAttribute[] = {1.0f, 1.0f, 1.0f};

    private float DiffuesLight[] = {0.6f, 0.6f, 0.6f};

    //相机矩阵
    private float[] mViewMatrix = new float[16];
    //投影矩阵
    private float[] mProjectMatrix = new float[16];
    //rotate matrix
    private float[][] mRotateMatrix = new float[MAX_OBJ][];
    //rotate matrix inv
    private float[][] mInvRotateMatrix = new float[MAX_OBJ][];
    //rotate inv trans --- normal matrix
    private float[][] mTransInvRotateMatrix = new float[MAX_OBJ][];

    //attr uniform position
    //Rotation A, X, Y, Z
    private int uRotateMatrixLocation;
    //view matrix view position matrix
    private int uViewMatrixLocation;
    //matrix for normal vec
    private int uNormalMatrixLocation;
    //project Matrix for 1:1
    private int uProjectMatrixLocation;
    //light position (x,y,z)
    private int uLightPositionLocation;
    //light attribute (r,g,b)
    private int uLightAttributeLocation;
    //diffues ligth (dr,dg,db)
    private int uDiffuesLightLocation;
    //vertex position (x,y,z)
    private int aPositionLocation;
    //normal (nx,ny,nz)
    private int aNormalLocation;
    //ligthStrength f
    private int uLightStrengthLocation;
    //rotate or not
    private int rotateLabel = 1;

    private float LightStrength;


    public RenderChange(String VShader, String FShader,float [] VertexArray, float [] NormalArray) {
        vertexShaderStr = VShader;
        fragmentShaderStr = FShader;
        ShapeCoords[0] = VertexArray.clone();
        NormalCoords[0] = NormalArray.clone();

        vertexBuffer[0] = ByteBuffer.allocateDirect(ShapeCoords[0].length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer[0].put(ShapeCoords[0]);
        vertexBuffer[0].position(0);

        normalBuffer[0] = ByteBuffer.allocateDirect(NormalCoords[0].length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        normalBuffer[0].put(NormalCoords[0]);
        normalBuffer[0].position(0);
        mRotateMatrix[0] = new float[16];
        mInvRotateMatrix[0] = new float[16];
        mTransInvRotateMatrix[0] = new float[16];

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

        uViewMatrixLocation = GLES20.glGetUniformLocation(mProgram, "viewMatrix");
        uProjectMatrixLocation = GLES20.glGetUniformLocation(mProgram, "projectMatrix");
        uRotateMatrixLocation = GLES20.glGetUniformLocation(mProgram, "rotateMatrix");
        uNormalMatrixLocation = GLES20.glGetUniformLocation(mProgram, "normalMatrix");

        uLightPositionLocation = GLES20.glGetUniformLocation(mProgram, "LightPosition");
        uLightAttributeLocation = GLES20.glGetUniformLocation(mProgram, "LightAttribute");
        uDiffuesLightLocation = GLES20.glGetUniformLocation(mProgram, "DiffuesLight");
        uLightStrengthLocation = GLES20.glGetUniformLocation(mProgram, "LightStrength");

        aPositionLocation = GLES20.glGetAttribLocation(mProgram, "verPosition");
        aNormalLocation = GLES20.glGetAttribLocation(mProgram, "verNormal");

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
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 1, 200);

        //设置相机位置
        SetViewPosition(0,0,70f);
        SetLightPosition(0,0,50f);
    }

    public void SetViewPosition(float xx, float yy, float ddistance){
        Matrix.setLookAtM(
                mViewMatrix, 0,
                (float)(ddistance*Math.cos(yy)*Math.sin(xx)), (float)(ddistance*Math.sin(yy)), (float)(ddistance*Math.cos(yy)*Math.cos(xx)),
                0f, 0f, 0f, 0f, 1.0f, 0.0f
        );
    }

    public void SetLightPosition(float xx, float yy, float ddistance){
        LightPosition[0] = (float)(ddistance*Math.cos(yy)*Math.sin(xx));
        LightPosition[1] = (float)(ddistance*Math.sin(yy));
        LightPosition[2] = (float)(ddistance*Math.cos(yy)*Math.cos(xx));

        //calculate the matrix
//        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);

    }

    private int cnt = 90;
    @Override
    public void onDrawFrame(GL10 gl) {
        cnt+=rotateLabel;

        Matrix.setIdentityM(mRotateMatrix[0], 0);
        Matrix.translateM(mRotateMatrix[0], 0, XDelta, YDelta, ZDelta);
        Matrix.rotateM(mRotateMatrix[0], 0, cnt*1.0f, 0f, 1.0f, 0f);
        Matrix.invertM(mInvRotateMatrix[0], 0, mRotateMatrix[0], 0);
        Matrix.transposeM(mTransInvRotateMatrix[0], 0, mInvRotateMatrix[0], 0);

        //把颜色缓冲区设置为我们预设的颜色,并且清空缓存
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT |  GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);

        //将变换矩阵传入顶点渲染器
        GLES20.glUniformMatrix4fv(uViewMatrixLocation,1,false,mViewMatrix,0);
        GLES20.glUniformMatrix4fv(uProjectMatrixLocation,1,false,mProjectMatrix,0);
        GLES20.glUniformMatrix4fv(uRotateMatrixLocation,1,false,mRotateMatrix[0],0);
        GLES20.glUniformMatrix4fv(uNormalMatrixLocation,1,false,mTransInvRotateMatrix[0],0);

        GLES20.glUniform3fv(uLightPositionLocation, 1, LightPosition, 0);
        GLES20.glUniform3fv(uLightAttributeLocation, 1, LightAttribute, 0);
        GLES20.glUniform3fv(uDiffuesLightLocation, 1, DiffuesLight, 0);
        GLES20.glUniform1f(uLightStrengthLocation, LightStrength);


        //准备坐标数据
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer[0]);
        //启用顶点位置句柄
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        //准备法向量数据
        GLES20.glVertexAttribPointer(aNormalLocation, 3, GLES20.GL_FLOAT, false, 0, normalBuffer[0]);
        //启用顶点位置句柄
        GLES20.glEnableVertexAttribArray(aNormalLocation);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, ShapeCoords.length/3);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aNormalLocation);
    }

    public void setLightStrength(float s){
        LightStrength = s;
    }

    public void setSpin (int Spin){
        rotateLabel = Spin;
    }

    private float XDelta;
    private float YDelta;
    private float ZDelta;


    public void setXDelta(float xx){
        XDelta = xx;
    }

    public void setYDelta(float yy){
        YDelta = yy;
    }

    public void setZDelta(float zz){
        ZDelta = zz;
    }

}

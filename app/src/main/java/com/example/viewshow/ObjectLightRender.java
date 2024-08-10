package com.example.viewshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ObjectLightRender implements GLSurfaceView.Renderer {


    //一个Float占用4Byte
    private static final int BYTES_PER_FLOAT = 4;

    static final int MAX_OBJ = 10;
    static final int MAX_TEX = 20;

    //顶点位置缓存
    private FloatBuffer[] vertexBuffer = new FloatBuffer[MAX_OBJ];

    //法向量缓存
    private FloatBuffer[] normalBuffer = new FloatBuffer[MAX_OBJ];

    //贴图缓存
    private FloatBuffer[] texBuffer = new FloatBuffer[MAX_OBJ];

    //总物体数量
    private int total = 0;

    //渲染程序
    private int NoShadowsProgram;
    private int texProgram;
    private int DepthProgram;
    private int ShadowProgram;
    private int LRTexProgram;

    private float[]  ShapeCoords [] = new float [MAX_OBJ][];

    private float[][] NormalCoords= new float [MAX_OBJ][];

    private float[][] TexCoords   = new float [MAX_OBJ][];
    private int[] TexResouceID = new int [MAX_OBJ];

    private float[] LightPosition= {0f, 0f, 0f};
    private float[] LightAttribute = {1.0f, 1.0f, 1.0f};
    private float[] DiffuesLight = {0.6f, 0.6f, 0.6f};

    //相机矩阵
    private final float[] mViewMatrix = new float[16];  //Only One
    //投影矩阵
    private final float[] mProjectMatrix = new float[16]; // Only for Once
    //灯光矩阵
    private final float[] mLightViewMatrix = new float[16];

    //rotate matrix
    private final float[][] mRotateMatrix = new float[MAX_OBJ][16];
    //rotate matrix inv
    private final float[][] mInvRotateMatrix = new float[MAX_OBJ][16];
    //rotate inv trans --- normal matrix
    private final float[][] mTransInvRotateMatrix = new float[MAX_OBJ][16];

    //rotate or not
    private int rotateLabel[] = new int[MAX_OBJ];
    private int rotateCnt [] = new int[MAX_OBJ];

    private float[] XDelta = new float[MAX_OBJ];
    private float[] YDelta = new float[MAX_OBJ];
    private float[] ZDelta = new float[MAX_OBJ];

    private float LightStrength;

    private Context myContext;
    private int[] textureHandle = new int[MAX_TEX];
    private int[] textureResourceID = new int[MAX_TEX];
    private int texTotal = 0;
    private float[] ViewPosition = {0f, 0f, 0f, 0f, 0f, 0f};
    private float ViewHorizon;
    private float ViewVertical;
    private float FocusLength = 20f;
    private float[] LeftPosition = {0f, 0f, 0f, 0f, 0f, 0f};
    private float[] RightPosition = {0f, 0f, 0f, 0f, 0f, 0f};

    private int ScreenWidth = 0;
    private final int shandowSampleSacle = 2;
    private int ScreenHeight = 0;
    private float[][] scaleTimes = new float[10][3];
    static final String TAG = "Render";

    private float[] MatrixVertex = {
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };
    private FloatBuffer MatrixVertexBuffer;


    private final float epsilon = 1e-3f;
    private float bias = 0f;

    private float[] TwoMatrixVertex = {
            -1.0f, -1.0f, 0.0f,
            -epsilon, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            -epsilon, -1.0f, 0.0f,
            -epsilon, 1.0f, 0.0f,

            epsilon, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            epsilon, 1.0f, 0.0f,
            epsilon, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f
    };
    private FloatBuffer TwoMatrixVertexBuffer;

    public ObjectLightRender(final Context context) {
        myContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST); // 开启深度测试
        GLES20.glDepthFunc(GLES20.GL_LESS); //基准设置为 1.0，那么GL_LESS 则深度小余 1.0 的通过测试

        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
        GLES20.glPolygonOffset(0.5f, 1f);

//        GLES20.glEnable(GLES20.GL_CULL_FACE);
//        GLES20.glCullFace(GLES20.GL_FRONT);

        int vertexShaderId = ShaderUtils.compileVertexShader(NoShadowVertex);
        int fragmentShaderId = ShaderUtils.compileFragmentShader(NoShadowFrag);
        NoShadowsProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId);

        int vertexShaderId2 = ShaderUtils.compileVertexShader(TextureVertex);
        int fragmentShaderId2 = ShaderUtils.compileFragmentShader(TextureFrag);
        texProgram = ShaderUtils.linkProgram(vertexShaderId2, fragmentShaderId2);

        int vertexShaderId3 = ShaderUtils.compileVertexShader(DepthVertex);
        int fragmentShaderId3 = ShaderUtils.compileFragmentShader(DepthFrag);
        DepthProgram = ShaderUtils.linkProgram(vertexShaderId3, fragmentShaderId3);

        int vertexShaderId4 = ShaderUtils.compileVertexShader(ShadowVertex);
        int fragmentShaderId4 = ShaderUtils.compileFragmentShader(ShadowFrag);
        ShadowProgram = ShaderUtils.linkProgram(vertexShaderId4, fragmentShaderId4);


        int vertexShaderId5 = ShaderUtils.compileVertexShader(LRTexVertex);
        int fragmentShaderId5 = ShaderUtils.compileFragmentShader(LRTexFrag);
        LRTexProgram = ShaderUtils.linkProgram(vertexShaderId5, fragmentShaderId5);


        MatrixVertexBuffer = ByteBuffer.allocateDirect(MatrixVertex.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        MatrixVertexBuffer.put(MatrixVertex);
        MatrixVertexBuffer.position(0);

        TwoMatrixVertexBuffer = ByteBuffer.allocateDirect(TwoMatrixVertex.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        TwoMatrixVertexBuffer.put(TwoMatrixVertex);
        TwoMatrixVertexBuffer.position(0);

        textureHandle[0] = loadTexture(myContext, R.drawable.texture0);
        textureResourceID[0] = R.drawable.texture0;
        texTotal ++ ;

        textureHandle[1] = loadTexture(myContext, R.drawable.texture1);
        textureResourceID[1] = R.drawable.texture1;
        texTotal ++ ;

        textureHandle[2] = loadTexture(myContext, R.drawable.texture2);
        textureResourceID[2] = R.drawable.texture2;
        texTotal ++ ;

        textureHandle[3] = loadTexture(myContext, R.drawable.texture3);
        textureResourceID[3] = R.drawable.texture3;
        texTotal ++ ;

        textureHandle[4] = loadTexture(myContext, R.drawable.texture4);
        textureResourceID[4] = R.drawable.texture4;
        texTotal ++ ;
    }

    private float near = 1f;
    private float far = 500f;
    private final float EyeDistance = 2f;

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        ScreenWidth = width;
        ScreenHeight = height;
        float ratio=(float)width/height;
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, near, far);
    }

    public void ReCalcLeftRightPosition(){
        LeftPosition[0] = EyeDistance * (float)Math.sin(ViewHorizon) + ViewPosition[0];
        LeftPosition[1] = ViewPosition[1];
        LeftPosition[2] = - EyeDistance * (float)Math.cos(ViewHorizon) + ViewPosition[2];

        RightPosition[0] = - EyeDistance * (float)Math.sin(ViewHorizon) + ViewPosition[0];
        RightPosition[1] = ViewPosition[1];
        RightPosition[2] = + EyeDistance * (float)Math.cos(ViewHorizon) + ViewPosition[2];


        LeftPosition[3] = FocusLength * (float)Math.cos(ViewHorizon) * (float)Math.cos(ViewVertical) + ViewPosition[0];
        LeftPosition[4] = FocusLength * (float)Math.sin(ViewVertical) + ViewPosition[1];
        LeftPosition[5] = FocusLength * (float)Math.sin(ViewHorizon) * (float)Math.cos(ViewVertical) + ViewPosition[2];

        RightPosition[3] = FocusLength * (float)Math.cos(ViewHorizon) * (float)Math.cos(ViewVertical) + ViewPosition[0];
        RightPosition[4] = FocusLength * (float)Math.sin(ViewVertical) + ViewPosition[1];
        RightPosition[5] = FocusLength * (float)Math.sin(ViewHorizon) * (float)Math.cos(ViewVertical) + ViewPosition[2];

//        Log.v("RightView" , "From " + RightPosition[0] + ","+RightPosition[1] +","+RightPosition[2] + " To "+RightPosition[3]+","+RightPosition[4]+","+RightPosition[5]);
//        Log.v("LeftView" , "From " + LeftPosition[0] + ","+LeftPosition[1] +","+LeftPosition[2] + " To "+LeftPosition[3]+","+LeftPosition[4]+","+LeftPosition[5]);

    }

    public void SetViewPosition(float xx, float yy, float zz, float horizonRad, float verticalRad){
        ViewPosition[0] = xx;
        ViewPosition[1] = yy;
        ViewPosition[2] = zz;
        ViewPosition[3] = 1f * (float)Math.cos(horizonRad) * (float)Math.cos(verticalRad) + ViewPosition[0];
        ViewPosition[4] = 1f * (float)Math.sin(verticalRad) + ViewPosition[1];
        ViewPosition[5] = 1f * (float)Math.sin(horizonRad) * (float)Math.cos(verticalRad) + ViewPosition[2];
        ViewHorizon = horizonRad;
        ViewVertical = verticalRad;

//        Log.v("MidView" , "From " + ViewPosition[0] + ","+ViewPosition[1] +","+ViewPosition[2] + " To "+ViewPosition[3]+","+ViewPosition[4]+","+ViewPosition[5]);

        ReCalcLeftRightPosition();
    }

    public void SetLookAt(float x, float y, float z, float ax, float ay, float az){
        Matrix.setLookAtM(
                mViewMatrix, 0, x, y, z,
                ax, ay, az, 0f, 1.0f, 0.0f
        );
    }

    public void SetLightAt(float x, float y, float z){
        Matrix.setLookAtM(
                mLightViewMatrix, 0, x, y, z,
                0f, 0f, 0f, 0f, 1.0f, 0.0f
        );
    }

    public void setFocusLength(float x){
        FocusLength = x;
        ReCalcLeftRightPosition();
    }

    public void setBias(float x){
        bias = x;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ, int id){
        scaleTimes[id][0] = scaleX;
        scaleTimes[id][1] = scaleY;
        scaleTimes[id][2] = scaleZ;
    }

    public void SetLightPosition(float xx, float yy, float ddistance){
        LightPosition[0] = (float)(ddistance*Math.cos(yy)*Math.sin(xx));
        LightPosition[1] = (float)(ddistance*Math.sin(yy));
        LightPosition[2] = (float)(ddistance*Math.cos(yy)*Math.cos(xx));
        SetLightAt(LightPosition[0], LightPosition[1], LightPosition[2]);
    }

    public static int loadTexture(final Context context, final int resourceId){
//        Log.v(TAG,"Get Texture ID = " + String.valueOf(resourceId));
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        if(textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }

        if(textureHandle[0] == 0)
        {
            throw new RuntimeException("failed to load texture");
        }
//        Log.v("Show","Position of Handle is "+String.valueOf(textureHandle[0]));
        return textureHandle[0];
    }

    public int findResourceID(int ResourceID){
        for (int i=0; i<texTotal; ++i){
            if (textureResourceID[i] == ResourceID) return textureHandle[i];
        }
        throw new RuntimeException("failed to find the texture");
    }

    public int addObject(float [] VertexArray, float [] NormalArray, float [] TexArray, int ResourceID) {
        int nowID = total++;
        scaleTimes[nowID][0] = 1f;
        scaleTimes[nowID][1] = 1f;
        scaleTimes[nowID][2] = 1f;
        ShapeCoords[nowID] = VertexArray.clone();
        NormalCoords[nowID] = NormalArray.clone();
        TexCoords[nowID] = TexArray.clone();
        TexResouceID[nowID] = ResourceID;

        vertexBuffer[nowID] = ByteBuffer.allocateDirect(ShapeCoords[nowID].length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer[nowID].put(ShapeCoords[nowID]);
        vertexBuffer[nowID].position(0);

        normalBuffer[nowID] = ByteBuffer.allocateDirect(NormalCoords[nowID].length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        normalBuffer[nowID].put(NormalCoords[nowID]);
        normalBuffer[nowID].position(0);

        texBuffer[nowID] = ByteBuffer.allocateDirect(TexCoords[nowID].length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texBuffer[nowID].put(TexCoords[nowID]);
        texBuffer[nowID].position(0);

        return nowID;
    }

    public void DrawSceneWithOutShadows(){

        GLES20.glViewport(0, 0, ScreenWidth, ScreenHeight);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glUseProgram(NoShadowsProgram);
        int uViewMatrixLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "viewMatrix");
        int uProjectMatrixLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "projectMatrix");
        int uRotateMatrixLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "rotateMatrix");
        int uNormalMatrixLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "normalMatrix");

        int uLightPositionLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "LightPosition");
        int uLightAttributeLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "LightAttribute");
        int uDiffuesLightLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "DiffuesLight");
        int uLightStrengthLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "LightStrength");
        int uTextureLocation = GLES20.glGetUniformLocation(NoShadowsProgram, "Texture");

        int aTexLocation = GLES20.glGetAttribLocation(NoShadowsProgram, "verTextureCoordinates");
        int aPositionLocation = GLES20.glGetAttribLocation(NoShadowsProgram, "verPosition");
        int aNormalLocation = GLES20.glGetAttribLocation(NoShadowsProgram, "verNormal");

        for (int i=0; i<total; ++i) {

            rotateCnt[i] += rotateLabel[i];
            Matrix.setIdentityM(mRotateMatrix[i], 0);
            Matrix.translateM(mRotateMatrix[i], 0, XDelta[i], YDelta[i], ZDelta[i]);
            Matrix.rotateM(mRotateMatrix[i], 0, rotateCnt[i] * 1.0f, 0f, 1.0f, 0f);
            Matrix.scaleM(mRotateMatrix[i], 0, scaleTimes[i][0], scaleTimes[i][1], scaleTimes[i][2]);
            Matrix.invertM(mInvRotateMatrix[i], 0, mRotateMatrix[i], 0);
            Matrix.transposeM(mTransInvRotateMatrix[i], 0, mInvRotateMatrix[i], 0);

            GLES20.glUniformMatrix4fv(uViewMatrixLocation, 1, false, mViewMatrix, 0);
            GLES20.glUniformMatrix4fv(uProjectMatrixLocation, 1, false, mProjectMatrix, 0);
            GLES20.glUniformMatrix4fv(uRotateMatrixLocation, 1, false, mRotateMatrix[i], 0);
            GLES20.glUniformMatrix4fv(uNormalMatrixLocation, 1, false, mTransInvRotateMatrix[i], 0);

            GLES20.glUniform3fv(uLightPositionLocation, 1, LightPosition, 0);
            GLES20.glUniform3fv(uLightAttributeLocation, 1, LightAttribute, 0);
            GLES20.glUniform3fv(uDiffuesLightLocation, 1, DiffuesLight, 0);
            GLES20.glUniform1f(uLightStrengthLocation, LightStrength);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, findResourceID(TexResouceID[i]));
            GLES20.glUniform1i(uTextureLocation, 0);

            GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer[i]);
            GLES20.glVertexAttribPointer(aNormalLocation, 3, GLES20.GL_FLOAT, false, 0, normalBuffer[i]);
            GLES20.glVertexAttribPointer(aTexLocation, 2, GLES20.GL_FLOAT, false, 0, texBuffer[i]);

            GLES20.glEnableVertexAttribArray(aPositionLocation);
            GLES20.glEnableVertexAttribArray(aNormalLocation);
            GLES20.glEnableVertexAttribArray(aTexLocation);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, ShapeCoords[i].length / 3);

            GLES20.glDisableVertexAttribArray(aPositionLocation);
            GLES20.glDisableVertexAttribArray(aNormalLocation);
            GLES20.glDisableVertexAttribArray(aTexLocation);
        }
    }

    public void DrawTex(int TextureID){

        GLES20.glViewport(0, 0, ScreenWidth, ScreenHeight);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUseProgram(texProgram);

        int aPositionLocation = GLES20.glGetAttribLocation(texProgram, "verPosition");
        int uTextureLocation = GLES20.glGetUniformLocation(texProgram, "Texture");

        GLES20.glVertexAttribPointer(
                aPositionLocation, 3, GLES20.GL_FLOAT,
                false, 0, MatrixVertexBuffer
        );
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureID);
        GLES20.glUniform1i(uTextureLocation, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, MatrixVertex.length / 3);
        GLES20.glDisableVertexAttribArray(aPositionLocation);

    }

    public void DrawLRTex(int LeftTexID, int RightTexID){
//        Log.v("Message", ScreenWidth + " " + ScreenHeight);
//        Log.v("rate", "" + 1.0f * ScreenWidth / ScreenHeight);

        GLES20.glViewport(0, 0, ScreenWidth, ScreenHeight);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);
        GLES20.glUseProgram(LRTexProgram);

        int aPositionLocation = GLES20.glGetAttribLocation(LRTexProgram, "verPosition");
        int uLeftTextureLocation = GLES20.glGetUniformLocation(LRTexProgram, "LeftTexture");
        int uRightTextureLocation = GLES20.glGetUniformLocation(LRTexProgram, "RightTexture");
        int uRateLocation = GLES20.glGetUniformLocation(LRTexProgram, "rateV");
        int uBiasLocation = GLES20.glGetUniformLocation(LRTexProgram, "biasV");

        GLES20.glVertexAttribPointer(
                aPositionLocation, 3, GLES20.GL_FLOAT,
                false, 0, TwoMatrixVertexBuffer
        );
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LeftTexID);
        GLES20.glUniform1i(uLeftTextureLocation, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, RightTexID);
        GLES20.glUniform1i(uRightTextureLocation, 1);


        GLES20.glUniform1f(uRateLocation, 1.0f * ScreenWidth / ScreenHeight);
        GLES20.glUniform1f(uBiasLocation, bias);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, TwoMatrixVertex.length / 3);
        GLES20.glDisableVertexAttribArray(aPositionLocation);

    }

    public void DrawSceneDepth(){

        GLES20.glViewport(0, 0, ScreenWidth * shandowSampleSacle, ScreenHeight * shandowSampleSacle);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);

        GLES20.glUseProgram(DepthProgram);
        int uViewMatrixLocation = GLES20.glGetUniformLocation(DepthProgram, "viewMatrix");
        int uProjectMatrixLocation = GLES20.glGetUniformLocation(DepthProgram, "projectMatrix");
        int uRotateMatrixLocation = GLES20.glGetUniformLocation(DepthProgram, "rotateMatrix");
        int uNearLocation = GLES20.glGetUniformLocation(DepthProgram, "aNear");
        int uFarLocation = GLES20.glGetUniformLocation(DepthProgram, "aFar");

        int aPositionLocation = GLES20.glGetAttribLocation(DepthProgram, "verPosition");


        for (int i=0; i<total; ++i) {

            rotateCnt[i] += rotateLabel[i];
            Matrix.setIdentityM(mRotateMatrix[i], 0);
            Matrix.rotateM(mRotateMatrix[i], 0, rotateCnt[i] * 1.0f, 0f, 1.0f, 0f);
            Matrix.translateM(mRotateMatrix[i], 0, XDelta[i], YDelta[i], ZDelta[i]);
            Matrix.scaleM(mRotateMatrix[i], 0, scaleTimes[i][0], scaleTimes[i][1], scaleTimes[i][2]);
            Matrix.invertM(mInvRotateMatrix[i], 0, mRotateMatrix[i], 0);
            Matrix.transposeM(mTransInvRotateMatrix[i], 0, mInvRotateMatrix[i], 0);

            GLES20.glUniformMatrix4fv(uViewMatrixLocation, 1, false, mViewMatrix, 0);
            GLES20.glUniformMatrix4fv(uProjectMatrixLocation, 1, false, mProjectMatrix, 0);
            GLES20.glUniformMatrix4fv(uRotateMatrixLocation, 1, false, mRotateMatrix[i], 0);

            GLES20.glUniform1f(uNearLocation, near);
            GLES20.glUniform1f(uFarLocation, far);

            GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer[i]);

            GLES20.glEnableVertexAttribArray(aPositionLocation);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, ShapeCoords[i].length / 3);

            GLES20.glDisableVertexAttribArray(aPositionLocation);
        }
    }

    public void DrawSceneShandows(int ShadowTextureID){

        GLES20.glViewport(0, 0, ScreenWidth, ScreenHeight);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);

        GLES20.glUseProgram(ShadowProgram);
        int uViewMatrixLocation = GLES20.glGetUniformLocation(ShadowProgram, "viewMatrix");
        int uProjectMatrixLocation = GLES20.glGetUniformLocation(ShadowProgram, "projectMatrix");
        int uRotateMatrixLocation = GLES20.glGetUniformLocation(ShadowProgram, "rotateMatrix");
        int uNormalMatrixLocation = GLES20.glGetUniformLocation(ShadowProgram, "normalMatrix");
        int uLightViewMatrixLocation = GLES20.glGetUniformLocation(ShadowProgram, "lightViewMatrix");

        int uLightPositionLocation = GLES20.glGetUniformLocation(ShadowProgram, "LightPosition");
        int uLightAttributeLocation = GLES20.glGetUniformLocation(ShadowProgram, "LightAttribute");
        int uDiffuesLightLocation = GLES20.glGetUniformLocation(ShadowProgram, "DiffuesLight");
        int uLightStrengthLocation = GLES20.glGetUniformLocation(ShadowProgram, "LightStrength");
        int uTextureLocation = GLES20.glGetUniformLocation(ShadowProgram, "Texture");
        int uShadowTexture = GLES20.glGetUniformLocation(ShadowProgram, "ShadowTexture");
        int uNearLocation = GLES20.glGetUniformLocation(ShadowProgram, "uNear");
        int uFarLocation = GLES20.glGetUniformLocation(ShadowProgram, "uFar");
        int uWidth = GLES20.glGetUniformLocation(ShadowProgram, "uWidth");
        int uHeight = GLES20.glGetUniformLocation(ShadowProgram, "uHeight");


        int aTexLocation = GLES20.glGetAttribLocation(ShadowProgram, "verTextureCoordinates");
        int aPositionLocation = GLES20.glGetAttribLocation(ShadowProgram, "verPosition");
        int aNormalLocation = GLES20.glGetAttribLocation(ShadowProgram, "verNormal");

        for (int i=0; i<total; ++i) {


            rotateCnt[i] += rotateLabel[i];
            Matrix.setIdentityM(mRotateMatrix[i], 0);
            Matrix.rotateM(mRotateMatrix[i], 0, rotateCnt[i] * 1.0f, 0f, 1.0f, 0f);
            Matrix.translateM(mRotateMatrix[i], 0, XDelta[i], YDelta[i], ZDelta[i]);
            Matrix.scaleM(mRotateMatrix[i], 0, scaleTimes[i][0], scaleTimes[i][1], scaleTimes[i][2]);
            Matrix.invertM(mInvRotateMatrix[i], 0, mRotateMatrix[i], 0);
            Matrix.transposeM(mTransInvRotateMatrix[i], 0, mInvRotateMatrix[i], 0);

            GLES20.glUniformMatrix4fv(uViewMatrixLocation, 1, false, mViewMatrix, 0);
            GLES20.glUniformMatrix4fv(uProjectMatrixLocation, 1, false, mProjectMatrix, 0);
            GLES20.glUniformMatrix4fv(uRotateMatrixLocation, 1, false, mRotateMatrix[i], 0);
            GLES20.glUniformMatrix4fv(uNormalMatrixLocation, 1, false, mTransInvRotateMatrix[i], 0);
            GLES20.glUniformMatrix4fv(uLightViewMatrixLocation, 1, false, mLightViewMatrix, 0);

            GLES20.glUniform3fv(uLightPositionLocation, 1, LightPosition, 0);
            GLES20.glUniform3fv(uLightAttributeLocation, 1, LightAttribute, 0);
            GLES20.glUniform3fv(uDiffuesLightLocation, 1, DiffuesLight, 0);
            GLES20.glUniform1f(uLightStrengthLocation, LightStrength);

            GLES20.glUniform1f(uNearLocation, near);
            GLES20.glUniform1f(uFarLocation, far);
            GLES20.glUniform1f(uWidth, ScreenWidth);
            GLES20.glUniform1f(uHeight, ScreenHeight);


            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, findResourceID(TexResouceID[i]));
            GLES20.glUniform1i(uTextureLocation, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ShadowTextureID);
            GLES20.glUniform1i(uShadowTexture, 1);

            GLES20.glVertexAttribPointer(aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer[i]);
            GLES20.glVertexAttribPointer(aNormalLocation, 3, GLES20.GL_FLOAT, false, 0, normalBuffer[i]);
            GLES20.glVertexAttribPointer(aTexLocation, 2, GLES20.GL_FLOAT, false, 0, texBuffer[i]);

            GLES20.glEnableVertexAttribArray(aPositionLocation);
            GLES20.glEnableVertexAttribArray(aNormalLocation);
            GLES20.glEnableVertexAttribArray(aTexLocation);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, ShapeCoords[i].length / 3);

            GLES20.glDisableVertexAttribArray(aPositionLocation);
            GLES20.glDisableVertexAttribArray(aNormalLocation);
            GLES20.glDisableVertexAttribArray(aTexLocation);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        //
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        int [] framebuffers = new int[1];
        GLES20.glGenFramebuffers(1, framebuffers, 0);
        int framebuffer = framebuffers[0];
        if (framebuffer == 0){
            throw new RuntimeException("Failed to Gen Depth Frame Buffer");
        }

        int [] depthTexs = new int[1];
        GLES20.glGenTextures(1, depthTexs, 0);
        int depthTex = depthTexs[0];
        if (depthTex == 0){
            throw new RuntimeException("Failed to Gen Depth Color Tex");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTex);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_RGB, ScreenWidth * shandowSampleSacle, ScreenHeight * shandowSampleSacle,
                0, GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, null
        );

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        int [] depthRenderBuffers = new int[1];
        GLES20.glGenRenderbuffers(1, depthRenderBuffers, 0);
        int depthRenderBuffer = depthRenderBuffers[0];

        if (depthRenderBuffer == 0){
            throw new RuntimeException("Failed to Gen Depth RenderBuffer");
        }
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer);
        GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                ScreenWidth * shandowSampleSacle, ScreenHeight * shandowSampleSacle
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, depthRenderBuffer
        );
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, depthTex, 0
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, depthRenderBuffer
        );
        SetLookAt(LightPosition[0], LightPosition[1], LightPosition[2], 0f, 0f, 0f);
        DrawSceneDepth();
        // Draw the Depth from the lightposition



        //
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        int [] Leftframebuffers = new int[1];
        GLES20.glGenFramebuffers(1, Leftframebuffers, 0);
        int Leftframebuffer = Leftframebuffers[0];
        if (Leftframebuffer == 0){
            throw new RuntimeException("Failed to Gen Left Frame Buffer");
        }

        int [] LeftTexs = new int[1];
        GLES20.glGenTextures(1, LeftTexs, 0);
        int LeftTex = LeftTexs[0];
        if (LeftTex == 0){
            throw new RuntimeException("Failed to Gen Left Color Tex");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, LeftTex);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_RGB, ScreenWidth, ScreenHeight,
                0, GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, null
        );

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        int [] LeftBuffers = new int[1];
        GLES20.glGenRenderbuffers(1, LeftBuffers, 0);
        int LeftBuffer = LeftBuffers[0];

        if (LeftBuffer == 0){
            throw new RuntimeException("Failed to Gen Left RenderBuffer");
        }
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, LeftBuffer);
        GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                ScreenWidth, ScreenHeight
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, LeftBuffer
        );
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, Leftframebuffer);
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, LeftTex, 0
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, LeftBuffer
        );
        SetLookAt(LeftPosition[0], LeftPosition[1], LeftPosition[2], LeftPosition[3], LeftPosition[4], LeftPosition[5]);
        DrawSceneShandows(depthTex);
        // Draw the Tex from the leftPosition


        //
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        int [] Rightframebuffers = new int[1];
        GLES20.glGenFramebuffers(1, Rightframebuffers, 0);
        int Rightframebuffer = Rightframebuffers[0];
        if (Rightframebuffer == 0){
            throw new RuntimeException("Failed to Gen Right Frame Buffer");
        }

        int [] RightTexs = new int[1];
        GLES20.glGenTextures(1, RightTexs, 0);
        int RightTex = RightTexs[0];
        if (RightTex == 0){
            throw new RuntimeException("Failed to Gen Right Color Tex");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, RightTex);

        GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0,
                GLES20.GL_RGB, ScreenWidth, ScreenHeight,
                0, GLES20.GL_RGB,
                GLES20.GL_UNSIGNED_BYTE, null
        );

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        int [] RightBuffers = new int[1];
        GLES20.glGenRenderbuffers(1, RightBuffers, 0);
        int RightBuffer = RightBuffers[0];

        if (RightBuffer == 0){
            throw new RuntimeException("Failed to Gen Right RenderBuffer");
        }
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, RightBuffer);
        GLES20.glRenderbufferStorage(
                GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                ScreenWidth, ScreenHeight
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, RightBuffer
        );
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, Rightframebuffer);
        GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, RightTex, 0
        );

        GLES20.glFramebufferRenderbuffer(
                GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, RightBuffer
        );
        SetLookAt(RightPosition[0], RightPosition[1], RightPosition[2], RightPosition[3], RightPosition[4], RightPosition[5]);
        DrawSceneShandows(depthTex);
        // Draw the Tex from the rightPosition



        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
//        SetLookAt(ViewPosition[0], ViewPosition[1], ViewPosition[2], ViewPosition[3], ViewPosition[4], ViewPosition[5]);
//        DrawTex(depthTex);
        DrawLRTex(LeftTex, RightTex);


        GLES20.glDeleteRenderbuffers(1, depthRenderBuffers, 0);
        GLES20.glDeleteTextures(1, depthTexs, 0);
        GLES20.glDeleteFramebuffers(1, framebuffers, 0);

        GLES20.glDeleteRenderbuffers(1, LeftBuffers, 0);
        GLES20.glDeleteTextures(1, LeftTexs, 0);
        GLES20.glDeleteFramebuffers(1, Leftframebuffers, 0);

        GLES20.glDeleteRenderbuffers(1, RightBuffers, 0);
        GLES20.glDeleteTextures(1, RightTexs, 0);
        GLES20.glDeleteFramebuffers(1, Rightframebuffers, 0);

    }

    public void setLightStrength(float s){
        LightStrength = 100f*s;
    }

    public void setSpin (int SpinSpeed, int ID){
        rotateLabel[ID] = SpinSpeed;
    }

    public void setXDelta(float xx, int ID){
        XDelta[ID] = xx;
    }

    public void setYDelta(float yy, int ID){
        YDelta[ID] = yy;
    }

    public void setZDelta(float zz, int ID){
        ZDelta[ID] = zz;
    }

    public void setDelta(float x, float y, float z, int ID) {
        setXDelta(x, ID);
        setYDelta(y, ID);
        setZDelta(z, ID);
    }

    private final String NoShadowVertex =
            "attribute vec3 verPosition;\n" +
                    "attribute vec3 verNormal;\n" +
                    "attribute vec2 verTextureCoordinates;\n" +
                    "\n" +
                    "varying vec3 LightIntensity;\n" +
                    "varying vec2 verTextureCoord;\n" +
                    "\n" +
                    "uniform vec3 LightPosition;\n" +
                    "uniform vec3 LightAttribute;\n" +
                    "uniform vec3 DiffuesLight;\n" +
                    "uniform float LightStrength;\n" +
                    "\n" +
                    "uniform mat4 viewMatrix;\n" +
                    "uniform mat4 projectMatrix;\n" +
                    "uniform mat4 rotateMatrix;\n" +
                    "uniform mat4 normalMatrix;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    vec3 TrueNormal = normalize(vec3(normalMatrix *vec4(verNormal,1.0)));\n" +
                    "    vec3 TruePosition = vec3(rotateMatrix*vec4(verPosition,1.0));\n" +
                    "    vec3 lightVec = normalize(LightPosition - TruePosition);\n" +
                    "    float lightLength = length(LightPosition - TruePosition);\n" +
                    "    LightIntensity = LightAttribute * DiffuesLight * max(dot(lightVec, TrueNormal), 0.0)/lightLength*LightStrength;\n" +
                    "    verTextureCoord = verTextureCoordinates;\n" +
                    "    gl_Position  = projectMatrix*viewMatrix*rotateMatrix*vec4(verPosition, 1.0);\n" +
                    "}";
    private final String NoShadowFrag =
            "precision mediump float;\n" +
                    "uniform sampler2D Texture;\n" +
                    "varying vec2 verTextureCoord;\n" +
                    "varying vec3 LightIntensity;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = vec4(LightIntensity+vec3(0.2,0.2,0.2), 0.0) * texture2D(Texture,verTextureCoord);\n" +
                    "}";

    private final String TextureVertex =
            "attribute vec3 verPosition;\n" +
                    "\n" +
                    "varying vec2 UV;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    UV = vec2(verPosition);\n" +
                    "    gl_Position = vec4(verPosition,1.0);\n" +
                    "}";

    private  final String TextureFrag =
            "precision mediump float;\n" +
                    "\n" +
                    "varying vec2 UV;\n" +
                    "uniform sampler2D Texture;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D( Texture, UV*vec2(0.5,0.5) +vec2(0.5,0.5) );\n" +
                    "}\n";

    private final String DepthVertex =
            "attribute vec3 verPosition;\n" +
                    "uniform mat4 viewMatrix;\n" +
                    "uniform mat4 projectMatrix;\n" +
                    "uniform mat4 rotateMatrix;\n" +
                    "uniform float aNear;\n" +
                    "uniform float aFar;\n" +
                    "varying float depth;\n" +
                    "varying vec4 Position;\n" +
                    "varying vec3 TruePosition;\n" +
                    "void main(){\n" +
                    "    TruePosition = vec3(rotateMatrix*vec4(verPosition,1.0));\n" +
                    "    Position = viewMatrix*rotateMatrix*vec4(verPosition, 1.0);\n" +
                    "    depth =  1.0 - (-Position.z - aNear) / (aFar - aNear);\n" +
                    "    gl_Position  = projectMatrix*viewMatrix*rotateMatrix*vec4(verPosition, 1.0);\n" +
                    "}";
    private final String DepthFrag =
            "precision highp float;\n" +
                    "varying float depth;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = vec4(depth, depth, depth, 1.0);\n" +
                    "}";

    private final String ShadowVertex =
            "attribute vec3 verPosition;\n" +
                    "attribute vec3 verNormal;\n" +
                    "attribute vec2 verTextureCoordinates;\n" +
                    "\n" +
                    "varying vec3 LightIntensity;\n" +
                    "varying vec2 verTextureCoord;\n" +
                    "\n" +
                    "uniform vec3 LightPosition;\n" +
                    "uniform vec3 LightAttribute;\n" +
                    "uniform vec3 DiffuesLight;\n" +
                    "uniform float LightStrength;\n" +
                    "\n" +
                    "uniform mat4 viewMatrix;\n" +
                    "uniform mat4 projectMatrix;\n" +
                    "uniform mat4 rotateMatrix;\n" +
                    "uniform mat4 normalMatrix;\n" +
                    "\n" +
                    "uniform float uFar;\n" +
                    "uniform float uNear;\n" +
                    "uniform float uWidth;\n" +
                    "uniform float uHeight;\n" +
                    "uniform mat4 lightViewMatrix;\n" +
                    "varying vec2 lightCoord;\n" +
                    "varying float nowDepth;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    vec3 TrueNormal = normalize(vec3(normalMatrix *vec4(verNormal,1.0)));\n" +
                    "    vec3 TruePosition = vec3(rotateMatrix*vec4(verPosition,1.0));\n" +
                    "    vec3 lightVec = normalize(LightPosition - TruePosition);\n" +
                    "    float lightLength = length(LightPosition - TruePosition);\n" +
                    "    vec4 ViewPositionInLight = lightViewMatrix*rotateMatrix*vec4(verPosition, 1.0);\n" +
                    "    vec4 projectPosition = projectMatrix*ViewPositionInLight;\n" +
                    "\n" +
                    "    lightCoord = projectPosition.xy / projectPosition.w/2.0 + vec2(0.5,0.5);\n" +
                    "    nowDepth = 1.0 - (-ViewPositionInLight.z - uNear) / (uFar - uNear);\n" +
                    "\n" +
                    "    LightIntensity = LightAttribute * DiffuesLight * max(dot(lightVec, TrueNormal), 0.0)/lightLength*LightStrength;\n" +
                    "    verTextureCoord = verTextureCoordinates;\n" +
                    "    gl_Position  = projectMatrix*viewMatrix*rotateMatrix*vec4(verPosition, 1.0);\n" +
                    "\n" +
                    "}";

    private final String ShadowFrag =
            "precision highp float;\n" +
                    "uniform sampler2D Texture;\n" +
                    "uniform sampler2D ShadowTexture;\n" +
                    "varying vec2 lightCoord;\n" +
                    "varying vec2 verTextureCoord;\n" +
                    "varying vec3 LightIntensity;\n" +
                    "varying float nowDepth;\n" +
                    "void main() {\n" +
                    "    float depth = texture2D(ShadowTexture, lightCoord).g;\n" +
                    "    if (depth - 0.0025 < nowDepth){\n" +
                    "        gl_FragColor = vec4(LightIntensity+vec3(0.2,0.2,0.2), 0.0) * texture2D(Texture,verTextureCoord);\n" +
                    "    } else {\n" +
                    "        gl_FragColor = vec4(0.2, 0.2, 0.2, 0.0) * texture2D(Texture,verTextureCoord);\n" +
                    "    }\n" +
                    "}";

    private final String LRTexVertex =
            "attribute vec3 verPosition;\n" +
                    "uniform float rateV;\n" +
                    "uniform float biasV;\n" +
                    "varying float rateF;\n" +
                    "varying float biasF;\n" +
                    "varying vec2 UV;\n" +
                    "void main(){\n" +
                    "    rateF = rateV;\n" +
                    "    biasF = biasV;\n" +
                    "    UV = vec2(verPosition);\n" +
                    "    gl_Position = vec4(verPosition,1.0);\n" +
                    "}";

    private final String LRTexFrag =
            "precision mediump float;\n" +
                    "varying vec2 UV;\n" +
                    "varying float rateF;\n" +
                    "varying float biasF;\n" +
                    "uniform sampler2D LeftTexture;\n" +
                    "uniform sampler2D RightTexture;\n" +
                    "void main() {\n" +
                    "    if (UV.x > 0.0){\n" +
                    "//        gl_FragColor = vec4((UV*vec2(1.0, 0.5) +vec2(0.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(-biasF, 0) , 0.0, 1.0);\n" +
                    "        gl_FragColor = texture2D( RightTexture, (UV*vec2(1.0, 0.5) +vec2(0.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(-biasF, 0));\n" +
                    "    } else {\n" +
                    "\n" +
                    "//        gl_FragColor = vec4((UV*vec2(1.0, 0.5) +vec2(1.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(+biasF,0) , 0.0, 1.0);\n" +
                    "        gl_FragColor = texture2D( LeftTexture, (UV*vec2(1.0, 0.5) +vec2(1.0,0.5)) * vec2(0.5, 1.0) + vec2((rateF - 1.0) / 2.0 /rateF , 0.0) + vec2(+biasF, 0));\n" +
                    "    }\n" +
                    "}";

}

package com.example.viewshow;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderUtils {

    private static final String TAG = "ShaderUtils";

    /**
     * 编译顶点着色器
     * @param shaderCode
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * 编译片段着色器
     * @param shaderCode
     */
    public static int compileFragmentShader(String shaderCode) {
//        Log.v("fuck",shaderCode);
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译
     * @param type       顶点着色器:GLES30.GL_VERTEX_SHADER
     *                   片段着色器:GLES30.GL_FRAGMENT_SHADER
     * @param shaderCode
     */
    private static int compileShader(int type, String shaderCode) {
        //创建一个着色器
        final int shaderId = GLES20.glCreateShader(type);
        if (shaderId != 0) {
            GLES20.glShaderSource(shaderId, shaderCode);

//            Log.v("", GLES20.glGetShaderSource(shaderId));
            GLES20.glCompileShader(shaderId);
            //检测状态
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                String logInfo = GLES20.glGetShaderInfoLog(shaderId);
                System.err.println(logInfo);
                //创建失败
                GLES20.glDeleteShader(shaderId);
                return 0;
            }
            return shaderId;
        } else {
            //创建失败
            return 0;
        }
    }

    /**
     * 链接小程序
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        //创建一个空的OpenGLES程序
        final int programId = GLES20.glCreateProgram();
        if (programId != 0) {
            //将顶点着色器加入到程序
            GLES20.glAttachShader(programId, vertexShaderId);
            //将片元着色器加入到程序中
            GLES20.glAttachShader(programId, fragmentShaderId);
            //链接着色器程序
            GLES20.glLinkProgram(programId);
            final int[] linkStatus = new int[1];

            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String logInfo = GLES20.glGetProgramInfoLog(programId);
                System.err.println(logInfo);
                GLES20.glDeleteProgram(programId);
                return 0;
            }
            return programId;
        } else {
            throw new RuntimeException("Error creating program.");
        }
    }

    /**
     * 验证程序片段是否有效
     * @param programObjectId
     */
    public static boolean validProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] programStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, programStatus, 0);
        return programStatus[0] != 0;
    }

}

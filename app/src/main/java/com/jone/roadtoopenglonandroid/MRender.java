package com.jone.roadtoopenglonandroid;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jinux on 17-1-24.
 */
public class MRender implements GLSurfaceView.Renderer {
    public static final String TAG = "MRender";

    private static final String VERTEX_SHADER = "attribute vec4 vPosition;\n"
            + "varying vec4 vColor;\n"
            + "void main() {\n"
            + "  vColor = vPosition;\n"
            + "  gl_Position = vPosition;\n"
            + "  gl_PointSize = 100.0;\n"
            + "}";
    private static final String FRAGMENT_SHADER = "precision mediump float;\n"
            + "varying vec4 vColor;\n"
            + "void main() {\n"
            + "  gl_FragColor = vec4(1, 1, 1, 1);\n"
            + "}";

    private static final float[] VERTEX = {
            0, 0f, 0.0f,
            0.5f, 0f, 0f,
            0.5f, 0.5f, 0f,

            -0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0f,
    };
    private FloatBuffer mVertexBuffer;
    private int mProgram;
    private int mPositionHandle;

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);   // 创建一个着色器对象
        GLES20.glShaderSource(shader, shaderCode); // 设置着色器对象的源码， 是好像是C语言，但只是像而已，它叫ALSL
        GLES20.glCompileShader(shader); // 编译，是的，既然设置的是源码，就需要编译了
        return shader;
    }

    static void checkGLError(String op) {
        final int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, "CheckGLError: " + msg);
            throw new RuntimeException(msg);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated");
        loadVertex();
        loadProgram();
    }

    /**
     * 加载顶点数据
     */
    private void loadVertex() {
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0); // Android里OpenGL使用Buffer作为数据，而不是直接使用数组，至于buffer怎么使用以后会有专门的说明
    }

    /**
     * 加载程序
     */
    private void loadProgram() {
        mProgram = GLES20.glCreateProgram(); // 创建一个Program对象，此对象非java里的对象，而是OpenGL里的对象，返回一个int值作为该对象的引用
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER); // 加载顶点着色器，对顶点做处理
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER); // 加载颜色着色器，为什么叫颜色着色器，直译过来应该是片着色器呀？因为主要是作颜色变换
        GLES20.glAttachShader(mProgram, vertexShader); // 将着色器绑定到程序对象上
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram); // 连接，编译程序，为什么编译呢，你一会看到那个C语言大妈就知道了。
        IntBuffer result = IntBuffer.allocate(1);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, result);
        Log.e(TAG, "loadProgram: " + result.get(0));
        if (result.get(0) == 1) {
            Log.e("glGetProgramInfoLog", GLES20.glGetProgramInfoLog(mProgram));
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition"); // 从着色器程序的到属性的位置，从而可以向该属性设置值
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e(TAG, "onDrawFrame");
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram); // 使用程序，  还记的状态机吗？在调用这一句后，OpenGL相关的绘制操作就会基于这个Program

        GLES20.glEnableVertexAttribArray(mPositionHandle); // 刚才的顶点位置属性，先使能
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);  // 然后向这个属性设置数据，各参数什么意思呢？
        checkGLError("glVertexAttribPointer");

//        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 5); // 离散的线，（1,2） （3,4） 多余的一个点就剩下了，没有用
//        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, 5); //（1,2）一条线 每增加一个点都会跟之前的一个点连成线
//        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 5); // （1,2）一条先 每增加一个点都会跟之前的一个点连成线，并且最后的一个点跟最开始的一个点之间连成一条线，从而形成一个闭环，见名知意呀
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 5); // 绘制离散的三角形，（1,2,3）一个三角形，多余的点同上边GL_LINES，舍弃
        checkGLError("glDrawArrays");

        GLES20.glDisableVertexAttribArray(mPositionHandle); // 使顶点属性不可用，这也是，状态机的操作

        GLES20.glUseProgram(0); // 还原程序，不在使用 mProgram
    }
}

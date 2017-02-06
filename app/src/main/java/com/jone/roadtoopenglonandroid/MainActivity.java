package com.jone.roadtoopenglonandroid;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GLSurfaceView surfaceView = (GLSurfaceView)findViewById(R.id.surfaceView1);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(new MRender());
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}

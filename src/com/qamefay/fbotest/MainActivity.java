package com.qamefay.fbotest;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Qamefay on 13-5-22.
 */
public class MainActivity extends Activity {

    private MyGLSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new MyGLSurfaceView(this);
        setContentView(surfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (surfaceView != null)
            surfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surfaceView != null)
            surfaceView.onResume();
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private MyRenderer renderer;

        public MyGLSurfaceView(Context context) {
            super(context);
            setEGLContextClientVersion(2);
            renderer = new MyRenderer(context);
            setRenderer(renderer);
        }

        private float mPreviousX;

        private float mPreviousY;

        private float mPointerPreviousX;

        private float mPointerPreviousY;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX(0);
            float y = event.getY(0);
            float pointerX = -1;
            float pointerY = -1;
            if (event.getPointerCount() > 1) {
                pointerX = event.getX(1);
                pointerY = event.getY(1);
            }
            switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;
                renderer.setRotate(dx / 2, dy / 2, false);
                if (pointerX != -1 && pointerY != -1) {
                    dx = pointerX - mPointerPreviousX;
                    dy = pointerY - mPointerPreviousY;
                    renderer.setRotate(dx / 2, dy / 2, true);
                }
                break;
            }
            mPreviousX = x;
            mPreviousY = y;
            if (pointerX != -1 && pointerY != -1) {
                mPointerPreviousX = pointerX;
                mPointerPreviousY = pointerY;
            }
            return true;
        }
    }

    class MyRenderer implements Renderer {

        int[] fbo;

        int[] texture;

        /**
         * 用与FBO的深度测试
         */
        int[] fboDepth;

        Context context;

        float[] getVex(float width, float offestX, float offestY, float offestZ) {
            float x = width / 2;
            return new float[] {
                    //前面
                    -x + offestX, x + offestY, x + offestZ,
                    x + offestX, x + offestY, x + offestZ,
                    -x + offestX, -x + offestY, x + offestZ,
                    x + offestX, -x + offestY, x + offestZ,
                    //上面
                    -x + offestX, x + offestY, x + offestZ,
                    x + offestX, x + offestY, x + offestZ,
                    -x + offestX,x + offestY,-x + offestZ,
                    x + offestX,x + offestY,-x + offestZ,
                    //后面
                    -x + offestX,-x + offestY,-x + offestZ,
                    x + offestX,-x + offestY,-x + offestZ,
                    -x + offestX,x + offestY,-x + offestZ,
                    x + offestX,x + offestY,-x + offestZ,
                    //下面
                    x + offestX,-x + offestY,x + offestZ,
                    -x + offestX,-x + offestY,x + offestZ,
                    x + offestX,-x + offestY,-x + offestZ,
                    -x + offestX,-x + offestY,-x + offestZ,
                    //左面
                    -x + offestX,x + offestY,-x + offestZ,
                    -x + offestX,x + offestY,x + offestZ,
                    -x + offestX,-x + offestY,-x + offestZ,
                    -x + offestX,-x + offestY,x + offestZ,
                    // 右面
                    x + offestX,x + offestY,-x + offestZ,
                    x + offestX,x + offestY,x + offestZ,
                    x + offestX,-x + offestY,-x + offestZ,
                    x + offestX,-x + offestY,x + offestZ};
        }

        float[] getCoor() {
            return new float[] {
                    // 前面
                    0, 0, 1, 0, 0, 1, 1, 1,
                    // 上面
                    0, 0, 1, 0, 0, 1, 1, 1,
                    // 后面
                    0, 0, 1, 0, 0, 1, 1, 1,
                    // 下面
                    0, 0, 1, 0, 0, 1, 1, 1,
                    // 左面
                    0, 0, 1, 0, 0, 1, 1, 1,
                    // 右面
                    0, 0, 1, 0, 0, 1, 1, 1, };
        }

        public MyRenderer(Context context) {
            this.context = context;
            fbo = new int[1];
            fboDepth = new int[1];
            texture = new int[2];
        }

        float mBigAngleX = 0;

        float mBigAngleY = 0;

        float mSmallAngleX = 0;

        float mSmallAngleY = 0;

        void setRotate(float mAngleX, float mAngleY, boolean isBig) {
            if (isBig) {
                this.mBigAngleX += mAngleX;
                this.mBigAngleY += mAngleY;
                Matrix.setRotateM(rotateM, 0, this.mBigAngleX, 0, 1, 0);
                Matrix.rotateM(rotateM, 0, this.mBigAngleY, 1, 0, 0);
            } else {
                this.mSmallAngleX += mAngleX;
                this.mSmallAngleY += mAngleY;
                Matrix.setRotateM(fboRotateM, 0, this.mSmallAngleX, 0, 1, 0);
                Matrix.rotateM(fboRotateM, 0, this.mSmallAngleY, 1, 0, 0);
            }

        }

        /**
         * 用于FBO的清屏，绘制背景图
         */
        Cube fboCube;

        /**
         * FBO内小立方体
         */
        Cube smallCube;

        /**
         * 用于显示的大立方体
         */
        Cube bigCube;

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glGenFramebuffers(1, fbo, 0);
            GLES20.glGenRenderbuffers(1, fboDepth, 0);
            GLES20.glGenTextures(2, texture, 0);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
            // 绑定FBO深度
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fboDepth[0]);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                    GLES20.GL_DEPTH_COMPONENT16, 128, 128);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,
                    fboDepth[0]);

            // 生成FBO绘制的图，使用FBO绘制的东西将绘制在这个图上
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[1]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0,
                    Bitmap.createBitmap(128, 128, Config.ARGB_8888), 0);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                    GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                    texture[1], 0);
            int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status == GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.d("MainActivity", "success");
            } else {
                Log.d("MainActivity", "error");
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

            // 绑定背景图懒洋洋到texture[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, Bitmap
                    .createScaledBitmap(BitmapFactory.decodeResource(
                            context.getResources(), R.drawable.lanyangyang),
                            128, 128, true), 0);
            fboCube = new Cube(new float[] { -1, 1, 0, 1, 1, 0, -1, -1, 0, 1,
                    -1, 0 });
            fboCube.setTexture(texture[0],
                    new float[] { 0, 0, 1, 0, 0, 1, 1, 1 });
            smallCube = new Cube(getVex(0.5f, 0, 0, 0));
            smallCube.setColor(new float[] { 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f });

            bigCube = new Cube(getVex(1, 0, 0, 0));
            bigCube.setTexture(texture[1], getCoor());
            setRotate(0, 45, true);
            setRotate(0, 0, false);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }

        private int width;

        private int height;

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            if (width == 0 || height == 0)
                return;
            this.width = width;
            this.height = height;
            ratio = (float) width / height;
        }

        float[] mvpMatrix = new float[16];

        float[] proMatrix = new float[16];

        float[] modelMatrix = new float[16];

        float[] rotateM = new float[16];

        float ratio = 0;

        float[] fboRotateM = new float[16];

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                    | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glViewport(0, 0, 128, 128);
            Matrix.frustumM(proMatrix, 0, -1, 1, -1, 1, 4, 10);
            Matrix.setLookAtM(modelMatrix, 0, 0, 0, 4, 0, 0, 0, 0, -1.0f, 0);
            Matrix.multiplyMM(mvpMatrix, 0, proMatrix, 0, modelMatrix, 0);
            fboCube.draw(mvpMatrix);
            Matrix.multiplyMM(mvpMatrix, 0, fboRotateM, 0, mvpMatrix, 0);
            smallCube.draw(mvpMatrix);

            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
                    | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glViewport(0, 0, width, height);
            Matrix.frustumM(proMatrix, 0, -ratio, ratio, -1, 1, 4, 10);
            Matrix.setLookAtM(modelMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1.0f, 0);
            Matrix.multiplyMM(mvpMatrix, 0, proMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(mvpMatrix, 0, rotateM, 0, mvpMatrix, 0);
            bigCube.draw(mvpMatrix);
        }
    }
}

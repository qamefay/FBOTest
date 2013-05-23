package com.qamefay.fbotest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * Created by Qamefay on 13-5-22.
 */
public class Cube {

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static FloatBuffer makeFloatBuffer(float[] buffer) {
        ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(buffer);
        floatBuffer.position(0);
        return floatBuffer;
    }
    private FloatBuffer vertexBuffer;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;"
                    + "attribute vec4 vPosition;"
                    + "attribute vec2 a_TexCoordinate;"
                    + "varying vec2 vTexCoordinate;"
                    + "void main() {"
                    + "  gl_Position = vPosition * uMVPMatrix;"
                    + "  vTexCoordinate = a_TexCoordinate;"
                    + "}";

    
    /**
     * 根据传进的颜色的alpha值判断是贴图还是颜色，默认颜色为-1
     */
    private final String fragmentShaderCode =
            "precision mediump float;"
                    + "uniform sampler2D uTexture;"
                    + "uniform vec4 vColor;"
                    + "varying vec2 vTexCoordinate;"
                    + "void main() {"
                    +   "float color = vColor.a;"
                    +   "if(color < 0.0){"
                    +       "gl_FragColor = texture2D(uTexture,vTexCoordinate);"
                    +    "}else{"
                    +       "gl_FragColor = vColor;"
                    +    "}"
                    + "}";

    private int mProgram;

    private float[] vertex;

    public Cube(float[] vertex) {
        this.vertex = vertex;
        vertexBuffer = Cube.makeFloatBuffer(vertex);
        int vertexShader = Cube.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = Cube.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);

        GLES20.glLinkProgram(mProgram);
    }

    private int mMVPMatrixHandle;

    private int mPositionHandle;

    private int mTextureHandle;

    private int mTextureCoordinateHandle;

    private int mColorHandle;

    float[] color = new float[]{-1,-1,-1,-1};

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        isShowTexture = false;
        this.color = color;
    }

    private FloatBuffer textureBuffer;

    public FloatBuffer getTextureBuffer() {
        return textureBuffer;
    }

    private int textureID;

    private boolean isShowTexture;

    public void setTexture(int id,float[] texCoorBuffer) {
        isShowTexture = true;
        textureID = id;
        this.textureBuffer = Cube.makeFloatBuffer(texCoorBuffer);
        this.color = new float[]{-1,-1,-1,-1};
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        if (isShowTexture) {
            mTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
            GLES20.glUniform1i(mTextureHandle, 0);
            if (textureBuffer != null) {
                textureBuffer.position(0);
                mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram,
                        "a_TexCoordinate");
                GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
                GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2,
                        GLES20.GL_FLOAT, false, 0, textureBuffer);
            }
        }
        for (int i = 0; i < vertex.length / 3; i+=4) {
            if(color[0] != -1) {
                int offest = i;
                if(i >= color.length) {
                    offest = (i / 4) % (color.length / 4) * 4;
                }
                GLES20.glUniform4fv(mColorHandle, 1, color, offest);
            }else {
                GLES20.glUniform4fv(mColorHandle, 1, color, 0);
            }
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, i, 4);
        }
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

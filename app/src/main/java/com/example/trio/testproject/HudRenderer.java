package com.example.trio.testproject;

import android.opengl.GLES10;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class HudRenderer implements GvrView.Renderer {

    private Cube mCube = new Cube();
    private float mCubeRotation;


    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        GLES10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        GLES10.glLoadIdentity();

        GLES10.glTranslatef(0.0f, 0.0f, -10.0f);
        GLES10.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);

        mCube.draw();

        GLES10.glLoadIdentity();

        mCubeRotation -= 0.15f;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        GLES10.glViewport(0, 0, width, height);
        GLES10.glMatrixMode(GL10.GL_PROJECTION);
        GLES10.glLoadIdentity();
        //GLU.gluPerspective(GLES10, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
        GLES10.glViewport(0, 0, width, height);

        GLES10.glMatrixMode(GL10.GL_MODELVIEW);
        GLES10.glLoadIdentity();
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        GLES10.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        GLES10.glClearDepthf(1.0f);
        GLES10.glEnable(GL10.GL_DEPTH_TEST);
        GLES10.glDepthFunc(GL10.GL_LEQUAL);

        GLES10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);
    }

    @Override
    public void onRendererShutdown() {

    }

    /*
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);

        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 0.0f, -10.0f);
        gl.glRotatef(mCubeRotation, 1.0f, 1.0f, 1.0f);

        mCube.draw();

        GLES10.glLoadIdentity();

        mCubeRotation -= 0.15f;
    }
*/
    private class Cube {

        private FloatBuffer mVertexBuffer;
        private FloatBuffer mColorBuffer;
        private ByteBuffer mIndexBuffer;

        private float vertices[] = {
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f
        };
        private float colors[] = {
                0.0f, 1.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.5f, 0.0f, 1.0f,
                1.0f, 0.5f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        };

        private byte indices[] = {
                0, 4, 5, 0, 5, 1,
                1, 5, 6, 1, 6, 2,
                2, 6, 7, 2, 7, 3,
                3, 7, 4, 3, 4, 0,
                4, 7, 6, 4, 6, 5,
                3, 0, 1, 3, 1, 2
        };

        public Cube() {
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            mVertexBuffer = byteBuf.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            mColorBuffer = byteBuf.asFloatBuffer();
            mColorBuffer.put(colors);
            mColorBuffer.position(0);

            mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
            mIndexBuffer.put(indices);
            mIndexBuffer.position(0);
        }

        public void draw() {
            GLES10.glFrontFace(GL10.GL_CW);

            GLES10.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
            GLES10.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);

            GLES10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            GLES10.glEnableClientState(GL10.GL_COLOR_ARRAY);

            GLES10.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE,
                    mIndexBuffer);

            GLES10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            GLES10.glDisableClientState(GL10.GL_COLOR_ARRAY);
        }
    }
}
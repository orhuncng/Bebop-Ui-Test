package com.example.trio.testproject;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;
import android.widget.FrameLayout;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class CanvasQuad {
    // The size of the quad is hardcoded for this sample and the quad doesn't have a model matrix so
    // these dimensions are used by translateClick() for touch interaction.
    private static final float WIDTH = 1f;
    private static final float HEIGHT = 1f;
    private static final float DISTANCE = 1f;

    private static final float[] MVP_MATRIX = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    // The number of pixels in this quad affect how Android positions Views in it. VideoUiView in VR
    // will be 1024 x 128 px in size which is similar to its 2D size. For Views that only have VR
    // layouts, using a number that results in ~10-15 px / degree is good.
    public static final int PX_PER_UNIT = 1024;

    // Standard vertex shader that passes through the texture data.
    private static final String[] vertexShaderCode = {
            "uniform mat4 uMvpMatrix;",
            // 3D position data.
            "attribute vec3 aPosition;",
            // 2D UV vertices.
            "attribute vec2 aTexCoords;",
            "varying vec2 vTexCoords;",

            // Standard transformation.
            "void main() {",
            "  gl_Position = uMvpMatrix * vec4(aPosition, 1);",
            "  vTexCoords = aTexCoords;",
            "}"
    };

    // Renders the texture of the quad using uAlpha for transparency.
    private static final String[] fragmentShaderCode = {
            // This is required since the texture data is GL_TEXTURE_EXTERNAL_OES.
            "#extension GL_OES_EGL_image_external : require",
            "precision mediump float;",

            // Standard texture rendering shader with extra alpha channel.
            "uniform samplerExternalOES uTexture;",
            "uniform float uAlpha;",
            "varying vec2 vTexCoords;",
            "void main() {",
            "  gl_FragColor.xyz = texture2D(uTexture, vTexCoords).xyz;",
            "  gl_FragColor.a = uAlpha;",
            "}"
    };

    // Program-related GL items. These are only valid if program != 0.
    private int program = 0;
    private int mvpMatrixHandle;
    private int positionHandle;
    private int textureCoordsHandle;
    private int textureHandle;
    private int textureId;
    private int alphaHandle;

    // Components used to manage the Canvas that the View is rendered to. These are only valid after
    // GL initialization. The client of this class acquires a Canvas from the Surface, writes to it
    // and posts it. This marks the Surface as dirty. The GL code then updates the SurfaceTexture
    // when rendering only if it is dirty.
    private SurfaceTexture displaySurfaceTexture;
    private Surface displaySurface;
    private final AtomicBoolean surfaceDirty = new AtomicBoolean();

    // The quad has 2 triangles built from 4 total vertices. Each vertex has 3 position & 2 texture
    // coordinates.
    private static final int POSITION_COORDS_PER_VERTEX = 3;
    private static final int TEXTURE_COORDS_PER_VERTEX = 2;
    private static final int COORDS_PER_VERTEX =
            POSITION_COORDS_PER_VERTEX + TEXTURE_COORDS_PER_VERTEX;
    private static final int BYTES_PER_COORD = 4;  // float.
    private static final int VERTEX_STRIDE_BYTES = COORDS_PER_VERTEX * BYTES_PER_COORD;

    // Interlaced position & texture data.
    private static final float[] vertexData = {
            -WIDTH / 2, -HEIGHT / 2, -DISTANCE,
            0, 1,
            WIDTH / 2, -HEIGHT / 2, -DISTANCE,
            1, 1,
            -WIDTH / 2, HEIGHT / 2, -DISTANCE,
            0, 0,
            WIDTH / 2, HEIGHT / 2, -DISTANCE,
            1, 0
    };
    private static final FloatBuffer vertexBuffer = GLUtils.createBuffer(vertexData);


    CanvasQuad() {
    }

    public static FrameLayout.LayoutParams getLayoutParams() {
        return new FrameLayout.LayoutParams((int) (WIDTH * PX_PER_UNIT), (int) (HEIGHT * PX_PER_UNIT));
    }

    public Canvas lockCanvas() {
        return displaySurface == null ? null : displaySurface.lockCanvas(null /* dirty Rect */);
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (canvas == null || displaySurface == null) {
            // glInit() hasn't run yet.
            return;
        }
        displaySurface.unlockCanvasAndPost(canvas);
        surfaceDirty.set(true);
    }


    void glInit() {
        if (program != 0) {
            return;
        }

        // Create the program.
        program = GLUtils.compileProgram(vertexShaderCode, fragmentShaderCode);
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMvpMatrix");
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        textureCoordsHandle = GLES20.glGetAttribLocation(program, "aTexCoords");
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture");
        textureId = GLUtils.glCreateExternalTexture();
        alphaHandle = GLES20.glGetUniformLocation(program, "uAlpha");
        GLUtils.checkGlError();

        // Create the underlying SurfaceTexture with the appropriate size.
        displaySurfaceTexture = new SurfaceTexture(textureId);
        displaySurfaceTexture.setDefaultBufferSize(
                (int) (WIDTH * PX_PER_UNIT), (int) (HEIGHT * PX_PER_UNIT));
        displaySurface = new Surface(displaySurfaceTexture);
    }


    void glDraw(float alpha) {
        // Configure shader.
        GLES20.glUseProgram(program);
        GLUtils.checkGlError();

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(textureCoordsHandle);
        GLUtils.checkGlError();

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, MVP_MATRIX, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureHandle, 0);
        GLES20.glUniform1f(alphaHandle, alpha);
        GLUtils.checkGlError();

        // Load position data.
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, VERTEX_STRIDE_BYTES, vertexBuffer);
        GLUtils.checkGlError();

        // Load texture data.
        vertexBuffer.position(POSITION_COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(textureCoordsHandle, TEXTURE_COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, VERTEX_STRIDE_BYTES, vertexBuffer);
        GLUtils.checkGlError();

        if (surfaceDirty.compareAndSet(true, false)) {
            // If the Surface has been written to, get the new data onto the SurfaceTexture.
            displaySurfaceTexture.updateTexImage();
        }

        // Render.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexData.length / COORDS_PER_VERTEX);
        GLUtils.checkGlError();

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordsHandle);
    }


    void glShutdown() {
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        }

        if (displaySurfaceTexture != null) {
            displaySurfaceTexture.release();
        }
    }
}

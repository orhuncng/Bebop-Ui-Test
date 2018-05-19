package com.trio.dronetest;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.view.Surface;
import com.trio.drone.R;

import java.nio.FloatBuffer;

public class OverlayTexture
{
    private static final int POSITION_COORDS_PER_VERTEX = 3;
    private static final int TEXTURE_COORDS_PER_VERTEX = 2;
    private static final int COORDS_PER_VERTEX =
            POSITION_COORDS_PER_VERTEX + TEXTURE_COORDS_PER_VERTEX;
    private static final int BYTES_PER_COORD = 4;
    private static final int VERTEX_COUNT = 4;
    private static final int VERTEX_STRIDE_BYTES = COORDS_PER_VERTEX * BYTES_PER_COORD;

    private static final float DEFAULT_ALPHA = 0.7f;

    private int program = 0;
    private int positionHandle;
    private int textureCoordsHandle;
    private int textureHandle;
    private int textureId;
    private int alphaHandle;
    private boolean hasAlpha;
    private float alpha = DEFAULT_ALPHA;

    private int width;
    private int height;

    private SurfaceTexture texture;
    private FloatBuffer vertexBuffer;

    public OverlayTexture(boolean hasAlpha, float depth, int screenWidth, int screenHeight,
            int width, int height)
    {
        this.hasAlpha = hasAlpha;

        this.width = width;
        this.height = height;

        width *= ((float) screenHeight) / screenWidth;

        if (width > height) {
            float xAspectCoeff = (1f - (((float) height) / width)) / 2f;
            vertexBuffer = GLUtils.createBuffer(new float[]{
                    -1f, -1f, depth, xAspectCoeff, 1,
                    1f, -1f, depth, 1 - xAspectCoeff, 1,
                    -1f, 1f, depth, xAspectCoeff, 0,
                    1f, 1f, depth, 1 - xAspectCoeff, 0
            });
        }
        else {
            float yAspectCoeff = (1f - (((float) width) / height)) / 2f;
            vertexBuffer = GLUtils.createBuffer(new float[]{
                    -1f, -1f, depth, 0, 1 - yAspectCoeff,
                    1f, -1f, depth, 1, 1 - yAspectCoeff,
                    -1f, 1f, depth, 0, yAspectCoeff,
                    1f, 1f, depth, 1, yAspectCoeff
            });
        }
    }

    Surface createSurface(Resources res, SurfaceTexture.OnFrameAvailableListener listener)
    {
        program = GLUtils.compileProgram(res, R.raw.overlay_vert,
                hasAlpha ? R.raw.overlay_alpha_frag : R.raw.overlay_frag);

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        textureCoordsHandle = GLES20.glGetAttribLocation(program, "aTexCoords");
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture");
        textureId = GLUtils.glCreateExternalTexture();

        if (hasAlpha) alphaHandle = GLES20.glGetUniformLocation(program, "uAlpha");

        texture = new SurfaceTexture(textureId);
        texture.setDefaultBufferSize(width, height);
        texture.setOnFrameAvailableListener(listener);

        return new Surface(texture);
    }

    void updateTexImage() { texture.updateTexImage(); }

    void draw()
    {
        GLES20.glUseProgram(program);
        GLUtils.checkGlError();

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(textureCoordsHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureHandle, 0);

        if (hasAlpha) GLES20.glUniform1f(alphaHandle, alpha);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(positionHandle, POSITION_COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, VERTEX_STRIDE_BYTES, vertexBuffer);

        vertexBuffer.position(POSITION_COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(textureCoordsHandle, TEXTURE_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, VERTEX_STRIDE_BYTES, vertexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
        GLUtils.checkGlError();

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordsHandle);
    }

    void shutdown()
    {
        if (program != 0) {
            GLES20.glDeleteProgram(program);
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        }

        if (texture != null) texture.release();
    }

    public void setAlpha(float alpha) { this.alpha = alpha; }

    public SurfaceTexture getTexture() { return texture; }
}

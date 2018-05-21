package com.trio.drone.vr;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.opengl.GLU.gluErrorString;

/**
 * GL utility methods.
 */
public class GLUtils
{
    private static final String TAG = "Video360.Utils";

    public static final int BYTES_PER_FLOAT = 4;

    /**
     * Debug builds should fail quickly. Release versions of the app should have this disabled.
     */
    private static final boolean HALT_ON_GL_ERROR = true;

    /**
     * Class only contains static methods.
     */
    private GLUtils()
    {
    }

    /**
     * Checks GLES20.glGetError and fails quickly if the state isn't GL_NO_ERROR.
     */
    public static void checkGlError()
    {
        int error = GLES20.glGetError();
        int lastError;
        if (error != GLES20.GL_NO_ERROR) {
            do {
                lastError = error;
                Log.e(TAG, "glError " + gluErrorString(lastError));
                error = GLES20.glGetError();
            } while (error != GLES20.GL_NO_ERROR);

            if (HALT_ON_GL_ERROR) {
                RuntimeException e = new RuntimeException("glError " + gluErrorString(lastError));
                Log.e(TAG, "Exception: ", e);
                throw e;
            }
        }
    }

    public static int loadGLShader(int type, String code)
    {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    private static int createShader(Resources res, int shaderId, int shaderType)
    {
        int shader;

        InputStream is = res.openRawResource(shaderId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            shader = GLES20.glCreateShader(shaderType);
            String s = sb.toString();
            GLES20.glShaderSource(shader, sb.toString());
            GLES20.glCompileShader(shader);
            checkGlError();
        }

        return shader;
    }

    public static int compileProgram(Resources res, int vertShaderId, int fragShaderId)
    {
        int vertexShader = createShader(res, vertShaderId, GLES20.GL_VERTEX_SHADER);
        int fragmentShader = createShader(res, fragShaderId, GLES20.GL_FRAGMENT_SHADER);

        int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        // Link and check for errors.
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String errorMsg = "Unable to link shader program: \n" + GLES20.glGetProgramInfoLog(
                    program);
            Log.e(TAG, errorMsg);
            if (HALT_ON_GL_ERROR) {
                throw new RuntimeException(errorMsg);
            }
        }
        checkGlError();

        return program;
    }

    /**
     * Allocates a FloatBuffer with the given data.
     */
    public static FloatBuffer createBuffer(float[] data)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = bb.asFloatBuffer();
        buffer.put(data);
        buffer.position(0);

        return buffer;
    }

    /**
     * Creates a GL_TEXTURE_EXTERNAL_OES with default configuration of GL_LINEAR filtering and
     * GL_CLAMP_TO_EDGE wrapping.
     */
    public static int glCreateExternalTexture()
    {
        int[] texId = new int[1];
        GLES20.glGenTextures(1, IntBuffer.wrap(texId));
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError();
        return texId[0];
    }
}

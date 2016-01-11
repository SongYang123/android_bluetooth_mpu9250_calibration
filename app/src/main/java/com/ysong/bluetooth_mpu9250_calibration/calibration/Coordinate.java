package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Coordinate extends CalibrationObject {

	private static final float AXIS_LEN = 200.0f;
	private static final float[] LIGHT_RED = {1.0f, 0.0f, 0.0f, 1.0f};
	private static final float[] LIGHT_GREEN = {0.0f, 1.0f, 0.0f, 1.0f};
	private static final float[] LIGHT_BLUE = {0.0f, 0.0f, 1.0f, 1.0f};
	private static final float[] DARK_RED = {0.5f, 0.0f, 0.0f, 1.0f};
	private static final float[] DARK_GREEN = {0.0f, 0.5f, 0.0f, 1.0f};
	private static final float[] DARK_BLUE = {0.0f, 0.0f, 0.5f, 1.0f};

	private FloatBuffer vertexBuffer;

	public Coordinate() {
		float[] vertex = new float[36];
		vertex[3] = vertex[10] = vertex[17] = AXIS_LEN;
		vertex[21] = vertex[28] = vertex[35] = -AXIS_LEN;
		vertexBuffer = ByteBuffer.allocateDirect(BYTE_PER_FLOAT * vertex.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(vertex).position(0);
	}

	@Override
	public void render(float[] mMVPMatrix) {
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mPositionHandle, COORD_PER_VERTEX, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * COORD_PER_VERTEX, vertexBuffer);
		GLES20.glUniform4fv(mColorHandle, 1, LIGHT_RED, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
		GLES20.glUniform4fv(mColorHandle, 1, LIGHT_GREEN, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2);
		GLES20.glUniform4fv(mColorHandle, 1, LIGHT_BLUE, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 4, 2);
		GLES20.glUniform4fv(mColorHandle, 1, DARK_RED, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);
		GLES20.glUniform4fv(mColorHandle, 1, DARK_GREEN, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 8, 2);
		GLES20.glUniform4fv(mColorHandle, 1, DARK_BLUE, 0);
		GLES20.glDrawArrays(GLES20.GL_LINES, 10, 2);
	}

	@Override
	public void release() {
		vertexBuffer.limit(0);
		vertexBuffer = null;
	}
}

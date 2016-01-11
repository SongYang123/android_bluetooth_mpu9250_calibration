package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Point extends CalibrationObject {

	private FloatBuffer vertexBuffer;
	private float[] color;

	public Point(float[] color) {
		vertexBuffer = ByteBuffer.allocateDirect(BYTE_PER_FLOAT * COORD_PER_VERTEX).order(ByteOrder.nativeOrder()).asFloatBuffer();
		this.color = color;
	}

	public void update(float[] vertex) {
		vertexBuffer.put(vertex).position(0);
	}

	@Override
	public void render(float[] mMVPMatrix) {
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mPositionHandle, COORD_PER_VERTEX, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * COORD_PER_VERTEX, vertexBuffer);
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	@Override
	public void release() {
		vertexBuffer.limit(0);
		vertexBuffer = null;
	}
}

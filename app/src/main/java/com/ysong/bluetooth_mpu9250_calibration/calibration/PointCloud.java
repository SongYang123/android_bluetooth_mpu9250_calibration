package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PointCloud extends CalibrationObject {

	private float[] vertex = new float[MAX_CLOUD * 3];
	private FloatBuffer vertexBuffer;
	private float[] color;

	public PointCloud(float[] color) {
		vertexBuffer = ByteBuffer.allocateDirect(BYTE_PER_FLOAT * COORD_PER_VERTEX * MAX_CLOUD).order(ByteOrder.nativeOrder()).asFloatBuffer();
		this.color = color;
	}

	public void update(float[][] vertex) {
		for (int i = 0; i < MAX_CLOUD; i++) {
			this.vertex[i * 3] = vertex[i][0];
			this.vertex[i * 3 + 1] = vertex[i][1];
			this.vertex[i * 3 + 2] = vertex[i][2];
		}
		vertexBuffer.put(this.vertex).position(0);
	}

	@Override
	public void render(float[] mMVPMatrix) {
		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glVertexAttribPointer(mPositionHandle, COORD_PER_VERTEX, GLES20.GL_FLOAT, false, BYTE_PER_FLOAT * COORD_PER_VERTEX, vertexBuffer);
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, MAX_CLOUD);
	}

	@Override
	public void release() {
		vertexBuffer.limit(0);
		vertexBuffer = null;
	}
}

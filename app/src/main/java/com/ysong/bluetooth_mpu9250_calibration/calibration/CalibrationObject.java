package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.opengl.GLES20;

public abstract class CalibrationObject {

	public static final int MAX_CLOUD = 30;

	protected static final int BYTE_PER_FLOAT = 4;
	protected static final int COORD_PER_VERTEX = 3;

	protected static int mMVPMatrixHandle;
	protected static int mPositionHandle;
	protected static int mColorHandle;

	public static void init(int programHandle) {
		mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
		mPositionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
		mColorHandle = GLES20.glGetUniformLocation(programHandle, "uColor");
	}

	public abstract void render(float[] mMVPMatrix);

	public abstract void release();
}

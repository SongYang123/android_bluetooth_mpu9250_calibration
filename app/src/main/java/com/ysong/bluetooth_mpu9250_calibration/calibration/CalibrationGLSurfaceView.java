package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CalibrationGLSurfaceView extends GLSurfaceView {

	private CalibrationGLRender mGLRender;
	private float mPrevX;
	private float mPrevY;

	public CalibrationGLSurfaceView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setEGLContextClientVersion(2);
		mGLRender = new CalibrationGLRender(context);
		setRenderer(mGLRender);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setPreserveEGLContextOnPause(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		float w = getWidth();

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float ay = -(float) 180 * (y - mPrevY) / w;
			float az = -(float) 180 * (x - mPrevX) / w;
			mGLRender.camMove(ay, az);
			requestRender();
		}

		mPrevX = x;
		mPrevY = y;
		return true;
	}

	public void zoom(float exp) {
		mGLRender.camZoom(exp);
		requestRender();
	}

	public void updatePointCloud(float[][] vertex) {
		mGLRender.updatePointCloud(vertex);
		requestRender();
	}

	public void updateCenter(float[] vertex) {
		mGLRender.updateCenter(vertex);
		requestRender();
	}

	public void updateConfirmCenter(float[] vertex) {
		mGLRender.updateConfirmCenter(vertex);
		requestRender();
	}

	public void release() {
		mGLRender.release();
	}
}

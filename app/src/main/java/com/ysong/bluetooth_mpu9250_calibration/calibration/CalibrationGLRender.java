package com.ysong.bluetooth_mpu9250_calibration.calibration;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ysong.bluetooth_mpu9250_calibration.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CalibrationGLRender implements GLSurfaceView.Renderer {

	private static final float FOV = (float) Math.PI / 3.0f;
	private static final float ANGLE_Y_LIMIT = (float) Math.PI * 5.0f / 12.0f;
	private static final float LEN_DEFAULT = 1000.0f / 2.0f / (float) Math.tan(FOV / 2.0);
	private static final float LEN_RANGE = 5.0f;

	private static final float[] color_cloud = {1.0f, 1.0f, 1.0f, 1.0f};
	private static final float[] color_center = {1.0f, 0.0f, 1.0f, 1.0f};
	private static final float[] color_confirm_center = {1.0f, 0.5f, 0.0f, 1.0f};

	private Context context;
	private float ratio;

	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	private float[] matCam = new float[16];
	private float len = LEN_DEFAULT;

	private Coordinate coordinate;
	private PointCloud pointCloud;
	private Point center;
	private Point confirmCenter;

	public CalibrationGLRender(Context context) {
		this.context = context;
		Matrix.setIdentityM(matCam, 0);
		coordinate = new Coordinate();
		pointCloud = new PointCloud(color_cloud);
		center = new Point(color_center);
		confirmCenter = new Point(color_confirm_center);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vertexShaderHandle, loadShader(R.raw.vertex_shader));
		GLES20.glCompileShader(vertexShaderHandle);
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fragmentShaderHandle, loadShader(R.raw.fragment_shader));
		GLES20.glCompileShader(fragmentShaderHandle);
		int programHandle = GLES20.glCreateProgram();
		GLES20.glAttachShader(programHandle, vertexShaderHandle);
		GLES20.glAttachShader(programHandle, fragmentShaderHandle);
		GLES20.glLinkProgram(programHandle);
		GLES20.glUseProgram(programHandle);
		CalibrationObject.init(programHandle);

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);

		GLES20.glLineWidth(2.0f);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		coordinate.render(mMVPMatrix);
		pointCloud.render(mMVPMatrix);
		center.render(mMVPMatrix);
		confirmCenter.render(mMVPMatrix);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		ratio = (float) width / height;
		Matrix.perspectiveM(mProjectionMatrix, 0, FOV / (float) Math.PI * 180.0f, ratio, len / LEN_RANGE, len * LEN_RANGE);
		Matrix.setLookAtM(mViewMatrix, 0, matCam[0] * len, matCam[1] * len, matCam[2] * len, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}

	public void camMove(float ay, float az) {
		float[] matCamTemp = new float[16];
		float[] matZ = new float[16];
		Matrix.setRotateM(matZ, 0, az, 0.0f, 0.0f, 1.0f);
		Matrix.multiplyMM(matCamTemp, 0, matZ, 0, matCam, 0);
		matCopy(matCamTemp, matCam);
		Matrix.rotateM(matCam, 0, ay, 0.0f, 1.0f, 0.0f);
		if (Math.abs(Math.atan2(matCam[2], matCam[10])) > ANGLE_Y_LIMIT) {
			matCopy(matCamTemp, matCam);
		}
		Matrix.setLookAtM(mViewMatrix, 0, matCam[0] * len, matCam[1] * len, matCam[2] * len, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}

	public void camZoom(float exp) {
		len = LEN_DEFAULT * (float) Math.pow(LEN_RANGE, exp);
		Matrix.perspectiveM(mProjectionMatrix, 0, FOV / (float) Math.PI * 180.0f, ratio, len / LEN_RANGE, len * LEN_RANGE);
		Matrix.setLookAtM(mViewMatrix, 0, matCam[0] * len, matCam[1] * len, matCam[2] * len, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
	}

	public void updatePointCloud(float[][] vertex) {
		pointCloud.update(vertex);
	}

	public void updateCenter(float[] vertex) {
		center.update(vertex);
	}

	public void updateConfirmCenter(float[] vertex) {
		confirmCenter.update(vertex);
	}

	public void release() {
		confirmCenter.release();
		center.release();
		pointCloud.release();
		coordinate.release();
	}

	private String loadShader(int resourceId) {
		InputStream inputStream = context.getResources().openRawResource(resourceId);
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		StringBuilder body = new StringBuilder();
		try {
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				body.append(nextLine);
				body.append('\n');
			}
		}
		catch (Exception e) {
			return null;
		}
		return body.toString();
	}

	private void matCopy(float[] src, float[] dst) {
		for (int i = 0; i < 16; i++) {
			dst[i] = src[i];
		}
	}
}

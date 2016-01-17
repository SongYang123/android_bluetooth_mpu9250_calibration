package com.ysong.bluetooth_mpu9250_calibration;

public class Calibration {

	private static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};
	private static final float DISTANCE_MIN = 20.0f;
	private static final int SAMPLE_MIN = 30;
	private static final float CONFIRM_DISTANCE_MIN = 5.0f;
	/* CONFIRM_MIN >= SAMPLE_MIN + CONFIRM_AVG_SIZE */
	private static final int CONFIRM_MIN = 100;
	private static final int CONFIRM_AVG_SIZE = 10;

	private float[][] point = new float[SAMPLE_MIN][3];
	private int pointIndex = 0;
	private float[] center;
	private float[] confirmCenter;
	private int confirmCount = 0;
	private float[][] confirmAvg = new float[CONFIRM_AVG_SIZE][3];

	private static void copyPoint(float[]src, float[]dst) {
		for (int i = 0; i < 3; i++) {
			dst[i] = src[i];
		}
	}

	private static float distance(float[] src, float[] dst) {
		float distance = 0;
		for (int i = 0; i < 3; i++) {
			distance += Math.pow(dst[i] - src[i], 2);
		}
		distance = (float) Math.sqrt(distance);
		return distance;
	}

	private static float[] calCenter(float[][] point) {
		float[] avgPoint = calAvgPoint(point);
		float[][] norPoint = calNorPoint(point, avgPoint);
		return calCentre(calLhsInv(calLhs(norPoint)), calRhs(norPoint), avgPoint);
	}

	private static float[] calAvgPoint(float[][] point) {
		float avg[] = new float[3];
		for (int i = 0; i < point.length; i++) {
			for (int j = 0; j < 3; j++) {
				avg[j] += point[i][j];
			}
		}
		for (int j = 0; j < 3; j++) {
			avg[j] /= point.length;
		}
		return avg;
	}

	private static float[][] calNorPoint(float[][] point, float[] avg) {
		float[][] norPoint = new float[SAMPLE_MIN][3];
		for (int i = 0; i < SAMPLE_MIN; i++) {
			for (int j = 0; j < 3; j++) {
				norPoint[i][j] = point[i][j] - avg[j];
			}
		}
		return norPoint;
	}

	private static float[] calRhs(float[][] norPoint) {
		float[] rhs = new float[3];
		float[] norPointSquareSum = new float[SAMPLE_MIN];
		for (int i = 0; i < SAMPLE_MIN; i++) {
			for (int j = 0; j < 3; j++) {
				norPointSquareSum[i] += Math.pow(norPoint[i][j], 2);
			}
		}
		for (int i = 0; i < SAMPLE_MIN; i++) {
			for (int j = 0; j < 3; j++) {
				rhs[j] += norPoint[i][j] * norPointSquareSum[i];
			}
		}
		for (int j = 0; j < 3; j++) {
			rhs[j] /= 2;
		}
		return rhs;
	}

	private static float[][] calLhs(float[][] norPoint) {
		float[][] lhs = new float[3][3];
		for (int i = 0; i < SAMPLE_MIN; i++) {
			for (int j = 0; j < 3; j++) {
				for (int k = 0; k < 3; k++) {
					lhs[j][k] += norPoint[i][j] * norPoint[i][k];
				}
			}
		}
		return lhs;
	}

	private static float det2(float m00, float m01, float m10, float m11) {
		return m00 * m11 - m01 * m10;
	}

	private static float[][] calLhsInv(float[][] lhs) {
		float inv[][] = new float[3][3];
		float det = lhs[0][0] * det2(lhs[1][1], lhs[1][2], lhs[2][1], lhs[2][2]);
		det -= lhs[0][1] * det2(lhs[1][0], lhs[1][2], lhs[2][0], lhs[2][2]);
		det += lhs[0][2] * det2(lhs[1][0], lhs[1][1], lhs[2][0], lhs[2][1]);
		if (det == 0) {
			return null;
		}
		inv[0][0] = det2(lhs[1][1], lhs[1][2], lhs[2][1], lhs[2][2]) / det;
		inv[0][1] = det2(lhs[0][2], lhs[0][1], lhs[2][2], lhs[2][1]) / det;
		inv[0][2] = det2(lhs[0][1], lhs[0][2], lhs[1][1], lhs[1][2]) / det;
		inv[1][0] = det2(lhs[1][2], lhs[1][0], lhs[2][2], lhs[2][0]) / det;
		inv[1][1] = det2(lhs[0][0], lhs[0][2], lhs[2][0], lhs[2][2]) / det;
		inv[1][2] = det2(lhs[0][2], lhs[0][0], lhs[1][2], lhs[1][0]) / det;
		inv[2][0] = det2(lhs[1][0], lhs[1][1], lhs[2][0], lhs[2][1]) / det;
		inv[2][1] = det2(lhs[0][1], lhs[0][0], lhs[2][1], lhs[2][0]) / det;
		inv[2][2] = det2(lhs[0][0], lhs[0][1], lhs[1][0], lhs[1][1]) / det;
		return inv;
	}

	private static float[] calCentre(float[][] lhsInv, float[] rhs, float[] avg) {
		float[] centre = new float[3];
		if (lhsInv == null) {
			return null;
		}
		for (int i = 0; i < 3; i++) {
			centre[i] = avg[i];
			for (int j = 0; j < 3; j++) {
				centre[i] += lhsInv[i][j] * rhs[j];
			}
		}
		return centre;
	}

	public void push(float[] point) {
		for (int i = 0; i < SAMPLE_MIN; i++) {
			if (distance(this.point[i], point) < DISTANCE_MIN) {
				return;
			}
		}
		copyPoint(point, this.point[pointIndex]);
		if (pointIndex == SAMPLE_MIN - 1) {
			pointIndex = 0;
		} else {
			pointIndex++;
		}
		center = calCenter(this.point);
		if (distance(ORIGIN, center) > CONFIRM_DISTANCE_MIN) {
			if (confirmCount < CONFIRM_MIN - CONFIRM_AVG_SIZE) {
				confirmCount++;
			} else if (confirmCount < CONFIRM_MIN) {
				copyPoint(center, confirmAvg[confirmCount + CONFIRM_AVG_SIZE - CONFIRM_MIN]);
				confirmCount++;
			}
			if (confirmCount == CONFIRM_MIN) {
				confirmCenter = calAvgPoint(confirmAvg);
			}
		} else {
			confirmCount = 0;
		}
	}

	public float[][] getPoint() {
		return point;
	}

	public float[] getCenter() {
		return center;
	}

	public float[] getConfirmCenter() {
		if (confirmCount == CONFIRM_MIN) {
			confirmCount = 0;
			return confirmCenter;
		} else {
			return null;
		}
	}
}

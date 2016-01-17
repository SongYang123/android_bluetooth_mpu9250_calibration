package com.ysong.bluetooth_mpu9250_calibration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ysong.bluetooth_mpu9250_calibration.calibration.CalibrationGLSurfaceView;

public class MainActivity extends AppCompatActivity {

	private static final long TIMEOUT = 50;

	private Toast toast = null;
	private CalibrationGLSurfaceView calibrationGLSurfaceView = null;
	private BluetoothSerial bluetoothSerial = null;
	private boolean threadEnabled = false;
	private Calibration calibration = null;

	private class DataThread implements Runnable {
		@Override
		public void run() {
			bluetoothSerial.flush();
			while (threadEnabled) {
				try {
					byte[] data = bluetoothSerial.read(TIMEOUT);
					if (data == null) {
						continue;
					}
					if (data.length == 0) {
						break;
					} else {
						float[] mag = getMag(data);
						calibration.push(mag);
						final float[] confirmCenter = calibration.getConfirmCenter();
						if (confirmCenter != null) {
							byte[] center = new byte[14];
							center[0] = 0;
							center[13] = 10;
							System.arraycopy(toHex16((int) confirmCenter[0]), 0, center, 1, 4);
							System.arraycopy(toHex16((int) confirmCenter[1]), 0, center, 5, 4);
							System.arraycopy(toHex16((int) confirmCenter[2]), 0, center, 9, 4);
							bluetoothSerial.write(center);
						}
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								calibrationGLSurfaceView.updatePointCloud(calibration.getPoint());
								calibrationGLSurfaceView.updateCenter(calibration.getCenter());
								if (confirmCenter != null) {
									calibrationGLSurfaceView.updateConfirmCenter(confirmCenter);
								}
							}
						});
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		calibrationGLSurfaceView = (CalibrationGLSurfaceView)findViewById(R.id.calibration_gl_surface_view);
		SeekBar zoom = (SeekBar)findViewById(R.id.zoom);
		bluetoothSerial = new BluetoothSerial(this);
		calibration = new Calibration();
		zoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
				calibrationGLSurfaceView.zoom(1.0f - progressValue / 50.0f);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		calibrationGLSurfaceView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		calibrationGLSurfaceView.onPause();
	}

	@Override
	protected void onDestroy() {
		calibrationGLSurfaceView.release();
		super.onDestroy();
	}

	public void onCxnHandler(View view) {
		try {
			bluetoothSerial.connect(6);
			toastShow("Connect success");
			threadEnabled = true;
			new Thread(new DataThread()).start();
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	public void onDxnHandler(View view) {
		threadEnabled = false;
		bluetoothSerial.poisonPill();
		try {
			bluetoothSerial.disconnect();
			toastShow("Disconnect success");
		} catch (Exception e) {
			toastShow(e.toString());
		}
	}

	private short byteToShort(byte lsb, byte msb) {
		return (short) ((msb & 0xFF) << 8 | (lsb & 0xFF));
	}

	private float[] getMag(byte[] data) {
		float[] mag = new float[3];
		for (int i = 0; i < 3; i++) {
			mag[i] = (float) byteToShort(data[i * 2], data[i * 2 + 1]);
		}
		return mag;
	}

	private byte[] toHex16(int x) {
		byte[] hex = new byte[4];
		hex[0] = (byte) ((x & 0x0F) + 48);
		hex[1] = (byte) (((x >> 4) & 0x0F) + 48);
		hex[2] = (byte) (((x >> 8) & 0x0F) + 48);
		hex[3] = (byte) (((x >> 12) & 0x0F) + 48);
		return hex;
	}

	private void toastShow(String str) {
		toast.setText(str);
		toast.show();
	}
}

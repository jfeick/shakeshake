package de.uni_weimar.benike.shakeshake;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.util.Arrays;

public class AccelerometerReader implements SensorEventListener {
    private Sensor accelerometer;
    private float[] accelerometerValues;
    private long lastMagnetometerTimestamp;
    private float[] lastMagnetometerVals;
    private long lastSystemTimestamp;
    private Sensor magnetometer;
    private SensorManager sensorManager;
    protected SensorService sensorService;

    public AccelerometerReader(SensorService sensorService) {
        this.accelerometerValues = new float[3];
        this.lastMagnetometerVals = new float[3];
        this.lastSystemTimestamp = 0;
        this.sensorService = sensorService;
        this.sensorManager = (SensorManager) sensorService.getSystemService("sensor");
        this.accelerometer = this.sensorManager.getDefaultSensor(1);
        this.sensorManager.registerListener(this, this.accelerometer, PreferenceManager.getDefaultSharedPreferences(sensorService.getContext()).getInt(Constants.PREF_SENSOR_DELAY, Constants.PREF_SENSOR_DELAY_IN_MICRO_SEC));
    }

    public void onSensorChanged(SensorEvent event) {
        if (this.sensorService != null) {
            long timestamp = System.currentTimeMillis();
            if (timestamp != this.lastSystemTimestamp || !Arrays.equals(event.values, this.accelerometerValues)) {
                if (event.sensor.getType() == 1) {
                    System.arraycopy(event.values, 0, this.accelerometerValues, 0, event.values.length);
                    this.lastMagnetometerVals = null;
                    this.sensorService.processAccelerometerEvent(this.accelerometerValues, this.lastMagnetometerVals, this.lastMagnetometerTimestamp, null, timestamp);
                    this.lastSystemTimestamp = timestamp;
                } else if (event.sensor.getType() == 2) {
                    System.arraycopy(event.values, 0, this.lastMagnetometerVals, 0, event.values.length);
                    this.lastMagnetometerTimestamp = SystemClock.elapsedRealtime();
                }
            }
        }
    }

    public void onDestroy() {
        if (this.sensorManager != null) {
            this.sensorManager.unregisterListener(this, this.accelerometer);
            this.sensorManager.unregisterListener(this, this.magnetometer);
            this.sensorManager.unregisterListener(this);
            this.sensorManager = null;
            this.accelerometer = null;
            this.magnetometer = null;
        }
        this.sensorService = null;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        this.sensorService.accuracyChanged();
    }
}

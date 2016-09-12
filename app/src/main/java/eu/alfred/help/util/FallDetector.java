package eu.alfred.help.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

public class FallDetector {

    private static final String TAG = "TAGFallDetector";

    public enum SensorDelay {
        SLOW,
        NORMAL,
        FAST,
        FASTEST
    }

    private final SensorManager senSensorManager;
    private final Sensor senAccelerometer;
    private final int BUFFER_SIZE = 50;
    private final ArrayList<Double> buffer;
    private int currentDelay;
    private boolean registered;
    private FallDetectorListener fallDetectorListener;

    public FallDetector(Context context) {
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        buffer = new ArrayList<>();
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer.add(0d);
        }

        this.currentDelay = SensorManager.SENSOR_DELAY_NORMAL;

        registered = false;
    }

    public synchronized void resume() {
        resume(currentDelay);
    }

    private synchronized void resume(int delay) {
        if (!registered) {
            registered = true;
            senSensorManager.registerListener(sensorEventListener, senAccelerometer, delay);
        }
    }

    public synchronized void pause() {
        if (registered) {
            senSensorManager.unregisterListener(sensorEventListener, senAccelerometer);
            registered = false;
        }
    }

    public synchronized void changeDelay(SensorDelay sensorDelay) {
        if (sensorDelay == null) return;
        boolean wasRegistered = registered;
        pause();
        if (sensorDelay == SensorDelay.NORMAL) {
            this.currentDelay = SensorManager.SENSOR_DELAY_UI;
        } else if (sensorDelay == SensorDelay.FAST) {
            this.currentDelay = SensorManager.SENSOR_DELAY_GAME;
        } else if (sensorDelay == SensorDelay.FASTEST) {
            this.currentDelay = SensorManager.SENSOR_DELAY_FASTEST;
        } else {
            this.currentDelay = SensorManager.SENSOR_DELAY_NORMAL;
        }
        if (wasRegistered) {
            resume();
        }

    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                double ax, ay, az;
                ax = event.values[0];
                ay = event.values[1];
                az = event.values[2];
                double aNorm = calculateAccelerationVector(ax, ay, az);
                addDataToBuffer(aNorm);
                if (detectedFall()) {
                    if (fallDetectorListener != null) {
                        fallDetectorListener.onFallDetected();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private boolean detectedFall() {
        if (buffer.get(buffer.size()-1) > 2f * SensorManager.GRAVITY_EARTH) {
            for (Double item : buffer) {
                Log.d(TAG, "detectedFall() 2  "+item);
                if (item < SensorManager.GRAVITY_EARTH * 0.38f) {
                    return true;
                }
            }
        }
        return false;
    }

    private double calculateAccelerationVector(double ax, double ay, double az) {
        return Math.sqrt(ax * ax + ay * ay + az * az);
    }

    private void addDataToBuffer(double aNorm) {
        if (buffer.size() >= BUFFER_SIZE) {
            buffer.remove(0);
        }
        buffer.add(aNorm);
    }

    public void setFallDetectorListener(FallDetectorListener fallDetectorListener) {
        this.fallDetectorListener = fallDetectorListener;
    }

    public interface FallDetectorListener {
        public void onFallDetected();
    }
}
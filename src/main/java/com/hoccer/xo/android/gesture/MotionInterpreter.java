package com.hoccer.xo.android.gesture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.hoccer.xo.android.gesture.Gestures.Transaction;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MotionInterpreter implements SensorEventListener {

    private static final Logger LOG = Logger.getLogger(MotionInterpreter.class);

    //public static final int HISTORY_LENGTH = 20;
    public static final long GESTURE_EXCLUSION_TIMESPAN = 1500;
    private static final String LOG_TAG = "MotionInterpreter";

    /*
    public static final Map<Integer, String> AXIS_NAMES;

    static {

        HashMap<Integer, String> axisNames = new HashMap<Integer, String>(3);
        axisNames.put(SensorConstants.X_AXIS, "X");
        axisNames.put(SensorConstants.Y_AXIS, "Y");
        axisNames.put(SensorConstants.Z_AXIS, "Z");
        AXIS_NAMES = Collections.unmodifiableMap(axisNames);
    }
    */

    private final Context mContext;
    private MotionGestureListener mListener;
    private List<Detector> mDetectors;

    private long mLastGestureTime;

    private final FeatureHistory mFeatureHistory;
    //private Transaction pMode;

    public MotionInterpreter(Transaction pMode, Context pContext,
                             MotionGestureListener pOnShakeListener) {
        LOG.debug(LOG_TAG + " Creating GestureInterpreter");
        mContext = pContext;

        mFeatureHistory = new FeatureHistory();

        mLastGestureTime = 0;
        setMode(pMode);

        setGestureListener(pOnShakeListener);
    }

    public FeatureHistory getFeatureHistory() {
        return mFeatureHistory;
    }

    public void addGestureDetector(Detector pDetector) {
        mDetectors.add(pDetector);
    }

    public void setGestureListener(MotionGestureListener pListener) {
        mListener = pListener;
    }

    public void deactivate() {
        if (mContext == null) {
            return;
        }

        SensorManager sensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);

        synchronized (mFeatureHistory) {
            mFeatureHistory.clear();
            mLastGestureTime = 0;
        }
    }

    public void activate() {
        if (mContext == null) {
            return;
        }

        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        handleSensorChange(event.values, event.timestamp / 1000000);
    }

    public void handleSensorChange(float[] pValues, long pTimestamp) {
        handleSensorChange(new Vec3D(pValues[0], pValues[1], pValues[2]), pTimestamp);
    }

    public void handleSensorChange(Vec3D pMeasurement, long pTimestamp) {

        synchronized (mFeatureHistory) {

            mFeatureHistory.add(pMeasurement, pTimestamp);

            for (Detector detector : mDetectors) {
                handleGesture(pTimestamp, detector.detect(mFeatureHistory));
            }

        }

    }

    private void handleGesture(long pTimestamp, int pGesture) {
        if (pGesture != Gestures.NO_GESTURE
                && (pTimestamp - mLastGestureTime > GESTURE_EXCLUSION_TIMESPAN)) {

            mListener.onMotionGesture(pGesture);
            LOG.debug(LOG_TAG + " Gesture detected: " + Gestures.GESTURE_NAMES.get(pGesture));

            mLastGestureTime = pTimestamp;
            mFeatureHistory.clear();
        }
    }

    public void setMode(Transaction pMode) {

        mDetectors = new ArrayList<Detector>();

        if (pMode == Transaction.SHARE || pMode == Transaction.SHARE_AND_RECEIVE) {
            addGestureDetector(new ThrowDetector());
        }
        if (pMode == Transaction.RECEIVE || pMode == Transaction.SHARE_AND_RECEIVE) {
            addGestureDetector(new CatchDetector());
        }
    }

}

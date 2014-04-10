package com.hoccer.xo.android.gesture;

import android.hardware.SensorManager;

public class ThrowDetector implements Detector {
    
    private static final long   TIMESPAN_OF_INTEREST = 500;
    
    @Override
    public int detect(FeatureHistory pHistory) {
        
        FeaturePattern yFeaturePattern = pHistory.getFeaturePattern(TIMESPAN_OF_INTEREST,
                SensorManager.DATA_Y);
        
        if (yFeaturePattern.endsWith("" + "up>")) {
            
            if (pHistory.wasHigherThan(0, 10, SensorManager.DATA_Y)) {
                
                if (pHistory.wasLowerThan(-19, TIMESPAN_OF_INTEREST, SensorManager.DATA_Y)) {
                    return Gestures.THROW;
                }
            }
        }
        
        return Gestures.NO_GESTURE;
    }
    
}

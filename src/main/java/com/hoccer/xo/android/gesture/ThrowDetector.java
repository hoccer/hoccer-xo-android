package com.hoccer.xo.android.gesture;


public class ThrowDetector implements Detector {
    
    private static final long   TIMESPAN_OF_INTEREST = 500;
    
    @Override
    public int detect(FeatureHistory pHistory) {
        
        FeaturePattern yFeaturePattern = pHistory.getFeaturePattern(TIMESPAN_OF_INTEREST, SensorConstants.X_AXIS);
        
        if (yFeaturePattern.endsWith("" + "up>")) {
            
            if (pHistory.wasHigherThan(0, 10, SensorConstants.Y_AXIS)) {
                
                if (pHistory.wasLowerThan(-19, TIMESPAN_OF_INTEREST, SensorConstants.Y_AXIS)) {
                    return Gestures.THROW;
                }
            }
        }
        
        return Gestures.NO_GESTURE;
    }
    
}

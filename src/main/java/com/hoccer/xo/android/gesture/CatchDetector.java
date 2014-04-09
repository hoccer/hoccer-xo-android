package com.hoccer.xo.android.gesture;

import android.hardware.SensorManager;

public class CatchDetector implements Detector {

    @Override
    public int detect(FeatureHistory pHistory) {

        if (uprightCatch(pHistory)) {
            return Gestures.CATCH;
        }

        // if (facingUpCatch(pHistory)) {
        // return GestureInterpreter.CATCH;
        // }

        // if (turnedCatch(pHistory)) {
        // return GestureInterpreter.CATCH;
        // }

        return Gestures.NO_GESTURE;
    }

    // private boolean turnedCatch(FeatureHistory pHistory) {
    //        
    // FeaturePattern featurePattern = pHistory.getFeaturePattern(200, SensorManager.DATA_Z);
    // if (featurePattern.matches("<fastup><flat><fastdown>")) {
    //            
    // LineFeature flat = pHistory.findLineAt(100, SensorManager.DATA_Z);
    // if (flat.isFlat() && flat.getLength() > 60) {
    //                
    // return true;
    // }
    // }
    //        
    // return false;
    // }

    private boolean facingUpCatch(FeatureHistory pHistory) {

        if (pHistory.wasLowerThan(-5, 400, SensorManager.DATA_Z)) {
            return false;
        }

        FeaturePattern featurePattern = pHistory.getFeaturePattern(400, SensorManager.DATA_X);
        if (featurePattern.matches("*<*up>*<*down>*<*up>*<*down>*")) {
            return false;
        }

        if (pHistory.wasHigherThan(15, 400, SensorManager.DATA_Z)) {

            if (pHistory.wasLowerThan(5, 400, SensorManager.DATA_Z)) {

                featurePattern = pHistory.getFeaturePattern(400, SensorManager.DATA_Z);

                if (featurePattern.matches("<*down>*<*up>*<flat>")) {
                    LineFeature line = pHistory.findLineAt(400, SensorManager.DATA_Z);
                    if (line.getLength() > 40) {
                        return true;
                    }
                }

            }
        }

        return false;
    }

    private boolean uprightCatch(FeatureHistory pHistory) {
        if (pHistory.isAtValue(9.81f, 2f, SensorManager.DATA_Y)) {

            if (pHistory.wasLowerThan(2, 400, SensorManager.DATA_Y)) {

                FeaturePattern featurePattern = pHistory.getFeaturePattern(400,
                        SensorManager.DATA_Y);

                if (featurePattern.startsWith("<up>") || featurePattern.startsWith("<fastup>")) {
                    if (featurePattern.endsWith("<flat>") || featurePattern.endsWith("<down>")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

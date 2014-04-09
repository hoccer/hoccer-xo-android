package com.hoccer.xo.android.gesture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Gestures {

    private static final String              LOG_TAG    = "Gestures";

    public static final int                  NO_GESTURE = -1;
    public static final int                  THROW      = 0;
    public static final int                  CATCH      = 2;
    public static final int                  SWEEP_OUT  = 3;
    public static final int                  SWEEP_IN   = 4;
    public static final int                  SHAKE      = 5;
    public static final int                  DROP       = 6;
    public static final int                  PICK       = 7;

    public static final Map<Integer, String> GESTURE_NAMES;
    static {
        HashMap<Integer, String> gestureNames = new HashMap<Integer, String>(3);
        gestureNames.put(THROW, "Throw");
        gestureNames.put(CATCH, "Catch");
        gestureNames.put(SWEEP_OUT, "Sweep Out");
        gestureNames.put(SWEEP_IN, "Sweep In");
        gestureNames.put(DROP, "Drop");
        gestureNames.put(PICK, "Pick");
        GESTURE_NAMES = Collections.unmodifiableMap(gestureNames);
    }

    public enum Transaction {
        RECEIVE, SHARE, SHARE_AND_RECEIVE, LOCKED
    }
}

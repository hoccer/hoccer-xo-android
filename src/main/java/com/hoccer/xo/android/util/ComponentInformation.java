package com.hoccer.xo.android.util;

import android.content.ComponentName;

public class ComponentInformation {

    public ComponentName componentName;
    public int           match;

    public ComponentInformation(ComponentName pComponentName, int pMatch) {
        componentName = pComponentName;
        match = pMatch;
    }

    @Override
    public String toString() {
        return componentName.getClassName();
    }

}

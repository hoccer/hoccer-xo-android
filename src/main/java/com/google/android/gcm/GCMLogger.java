/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gcm;

import android.util.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Custom logger.
 */
class GCMLogger {

    private final String mTag;
    // can't use class name on TAG since size is limited to 23 chars
    private final String mLogPrefix;

    private final Logger mLogger;

    GCMLogger(String tag, String logPrefix, Class<?> clazz) {
        mTag = tag;
        mLogPrefix = logPrefix;
        mLogger = Logger.getLogger(clazz);
    }

    /**
     * Logs a message on logcat.
     *
     * @param priority logging priority
     * @param template message's template
     * @param args list of arguments
     */
    protected void log(int priority, String template, Object... args) {
        Level level = Level.INFO;
        switch (priority) {
            case Log.INFO:
                level = Level.INFO;
                break;
            case Log.ERROR:
                level = Level.ERROR;
                break;
            case Log.DEBUG:
                level = Level.DEBUG;
                break;
            case Log.VERBOSE:
                level = Level.DEBUG;
                break;
            case Log.WARN:
                level = Level.WARN;
                break;
            case Log.ASSERT:
                level = Level.FATAL;
                break;
        }

        if(mLogger.isEnabledFor(level)) {
            mLogger.log(level, String.format(template, args));
        }
    }
}

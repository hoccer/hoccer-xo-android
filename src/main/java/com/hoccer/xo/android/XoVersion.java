package com.hoccer.xo.android;

import android.content.Context;
import com.whitelabel.gw.release.R;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class XoVersion {

    private static final String UNINITIALIZED = "Uninitialized";

    private static final Logger LOG = Logger.getLogger(XoVersion.class);

    private static Properties GIT = null;

    private static String BRANCH = UNINITIALIZED;

    private static String BUILD_TIME = UNINITIALIZED;

    private static String COMMIT_ID = UNINITIALIZED;
    private static String COMMIT_ABBREV = UNINITIALIZED;
    private static String COMMIT_DESCRIBE = UNINITIALIZED;

    public static void initialize(Context context) {
        LOG.debug("initializing git properties");
        Properties git = new Properties();
        InputStream is = context.getResources().openRawResource(R.raw.git_properties);
        try {
            git.load(is);
            GIT = git;
        } catch (IOException e) {
            LOG.error("error loading git properties", e);
        }
        if(GIT != null) {
            BRANCH = GIT.getProperty("git.branch", UNINITIALIZED);
            BUILD_TIME = GIT.getProperty("git.build.time", UNINITIALIZED);
            COMMIT_ID = GIT.getProperty("git.commit.id", UNINITIALIZED);
            COMMIT_ABBREV = GIT.getProperty("git.commit.id.abbrev", UNINITIALIZED);
            COMMIT_DESCRIBE = GIT.getProperty("git.commit.id.describe", UNINITIALIZED);
        }
    }

    public static String getBranch() {
        return BRANCH;
    }

    public static String getBuildTime() {
        return BUILD_TIME;
    }

    public static String getCommitId() {
        return COMMIT_ID;
    }

    public static String getCommitAbbrev() {
        return COMMIT_ABBREV;
    }

    public static String getCommitDescribe() {
        return COMMIT_DESCRIBE;
    }

}

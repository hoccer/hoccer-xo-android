package com.hoccer.xo.android.gesture;

import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;

/*
import com.hoccer.android.Logger;
import com.hoccer.android.http.HttpClientException;
import com.hoccer.android.http.HttpHelper;
import com.hoccer.android.http.HttpServerException;
*/

public class FeatureHistory {

    //private static final Logger LOG = Logger.getLogger(FeatureHistory.class);
    //private static final String LOG_TAG = "FeatureHistory";

    private final LinkedList<LineFeature>[] mLineFeatureHistories;

    private long mStartTimeOfHistroy = -1;

    public FeatureHistory() {

        mLineFeatureHistories = new LinkedList[3];
        mLineFeatureHistories[0] = new LinkedList<LineFeature>();
        mLineFeatureHistories[1] = new LinkedList<LineFeature>();
        mLineFeatureHistories[2] = new LinkedList<LineFeature>();

    }

    public void add(Vec3D pMeasurement, long pTimestamp) {
        float[] mesurements = {pMeasurement.x, pMeasurement.y, pMeasurement.z};
        add(mesurements, pTimestamp);
    }

    public void add(float[] pMeasurements, long pTimestamp) {
        if (mStartTimeOfHistroy == -1) {
            mStartTimeOfHistroy = pTimestamp;
        }

        addMeasurmentsToLineFeatureHistory(pMeasurements, pTimestamp);
    }

    private void addMeasurmentsToLineFeatureHistory(float[] pMeasurements, long pTimestamp) {
        for (int i = 0; i < 3; i++) {
            LinkedList<LineFeature> lineFeatures = mLineFeatureHistories[i];
            Vec2D newPoint = new Vec2D(getDifferenceToStart(pTimestamp), pMeasurements[i]);

            if (lineFeatures.size() == 0) {
                add(new LineFeature(newPoint), i);
            } else if (!lineFeatures.getFirst().add(newPoint)) {
                Vec2D lastPoint = lineFeatures.getFirst().getNewest();
                add(new LineFeature(lastPoint, newPoint), i);
            }
        }
    }

    private float getDifferenceToStart(long pTimestamp) {

        return (pTimestamp - mStartTimeOfHistroy);
    }

    void add(LineFeature pFeature, int pAxis) {
        LinkedList<LineFeature> history = mLineFeatureHistories[pAxis];

        if (history.size() > 30) {
            history.removeLast();
        }

        history.addFirst(pFeature);
    }

    public void clear() {

        mLineFeatureHistories[0].clear();
        mLineFeatureHistories[1].clear();
        mLineFeatureHistories[2].clear();

        mStartTimeOfHistroy = -1;
    }

    public boolean hasPeak(long pTimespan, int pAxis) {
        FeaturePattern featurePattern = getFeaturePattern(pTimespan, pAxis);
        return featurePattern.matches("*<*up><*down>*");
    }

    public boolean isStartingToFall(long pTimespan, int pAxis) {
        FeaturePattern featurePattern = getFeaturePattern(pTimespan, pAxis);
        return hasPeak(pTimespan, pAxis) || featurePattern.matches("*<flat><*down>");
    }

    /**
     * returns the current feature pattern in given timespan (latest feature is right most, like you
     * would graph it)
     *
     * @return Pattern e.g. "<up><fastup><down><flat><fastdown>..."
     */
    public FeaturePattern getFeaturePattern(long pTimespan, int pAxis) {
        LinkedList<LineFeature> history = mLineFeatureHistories[pAxis];

        String pattern = "";
        long durationSum = 0;
        Iterator<LineFeature> itr = history.iterator();
        while (durationSum < pTimespan && itr.hasNext()) {
            LineFeature line = itr.next();
            durationSum += line.getLength();

            if (line.isFlat())
                pattern = "<flat>" + pattern;
            else if (line.isFastAscending())
                pattern = "<fastup>" + pattern;
            else if (line.isAscending())
                pattern = "<up>" + pattern;
            else if (line.isFastDescending())
                pattern = "<fastdown>" + pattern;
            else if (line.isDescending())
                pattern = "<down>" + pattern;
        }

        return new FeaturePattern(pattern);
    }

    public boolean isAtValue(float pTargetValue, float pAcceptedVarriance, int pAxis) {
        LineFeature line = findNewestLineOn(pAxis);
        if (line == null) {
            return false;
        }

        if (Math.abs(line.getNewest().y - pTargetValue) < pAcceptedVarriance) {
            return true;
        }

        return false;
    }

    public boolean wasLowerThan(float pBoundary, float pTimespan, int pAxis) {

        LinkedList<LineFeature> history = mLineFeatureHistories[pAxis];
        long durationSum = 0;
        Iterator<LineFeature> itr = history.iterator();
        while (durationSum < pTimespan && itr.hasNext()) {
            LineFeature line = itr.next();
            durationSum += line.getLength();

            if (line.getNewest().y < pBoundary || line.getLatest().y < pBoundary) {
                return true;
            }
        }

        // TODO this would be better but breaks with the existing throw detector
        // if (pBoundary > getMinimum(pTimespan, pAxis)) {
        // return true;
        // }

        return false;
    }

    public boolean wasHigherThan(float pBoundary, float pTimespan, int pAxis) {
        if (pBoundary <= getMaximum(pTimespan, pAxis)) {
            return true;
        }

        return false;
    }

    public boolean keepsBelow(float pBoundary, float pTimespan, int pAxis) {

        // if the timespan is not available we can not guarantee it has been below a given value
        if (findNewestLineOn(pAxis) != null && findNewestLineOn(pAxis).getNewest().x < pTimespan) {
            return false;
        }

        if (getMaximum(pTimespan, pAxis) < pBoundary)
            return true;

        return false;
    }

    public float getMaximum(float pTimespan, int pAxis) {
        return getExtrema(pTimespan, pAxis)[0];
    }

    public float getMinimum(float pTimespan, int pAxis) {
        return getExtrema(pTimespan, pAxis)[1];
    }

    public float[] getExtrema(float pTimespan, int pAxis) {

        LinkedList<LineFeature> history = mLineFeatureHistories[pAxis];
        long durationSum = 0;
        float maximum = -Float.MAX_VALUE;
        float minimum = Float.MAX_VALUE;
        Iterator<LineFeature> itr = history.iterator();
        while (durationSum < pTimespan && itr.hasNext()) {
            LineFeature line = itr.next();
            if ((history.getFirst().getNewest().x - pTimespan) > line.getLatest().x) {
                line = new LineFeature(line.getPointAtX(line.getNewest().x - pTimespan), line
                        .getNewest());
                durationSum += 100; // hacky exiting the loop
            }
            durationSum += line.getLength();

            if (line.getNewest().y > maximum)
                maximum = line.getNewest().y;
            if (line.getLatest().y > maximum) {
                maximum = line.getLatest().y;
            }

            if (line.getNewest().y < minimum)
                minimum = line.getNewest().y;
            if (line.getLatest().y < minimum) {
                minimum = line.getLatest().y;
            }
        }

        float[] extrema = {maximum, minimum};
        return extrema;
    }

    public LineFeature findNewestLineOn(int pAxis) {
        if (mLineFeatureHistories[pAxis].isEmpty()) {
            return null;
        }

        return mLineFeatureHistories[pAxis].getFirst();
    }

    public LineFeature findLineAt(long pPointInTime, int pAxis) {
        LinkedList<LineFeature> history = mLineFeatureHistories[pAxis];

        LineFeature line = null;
        long pointInTime = 0;
        Iterator<LineFeature> itr = history.iterator();
        while (pointInTime < pPointInTime) {
            if (!itr.hasNext()) {
                break;
            }
            line = itr.next();
            pointInTime += line.getLength();
        }

        return line;
    }

    /*
    public void storeGraph(String pFilename) throws HttpClientException, HttpServerException,
            IOException {
        storeGraph(new File(pFilename));
    }

    public void storeGraph(File pFile) throws HttpClientException, HttpServerException, IOException {

        String dataSeriesX = "";
        String dataSeriesY = "";
        String dataSeries = "";
        for (LinkedList<LineFeature> history : mLineFeatureHistories) {
            if (history.size() == 0) {
                continue;
            }

            for (LineFeature line : history) {
                Vec2D latestPoint = line.getLatest();
                Vec2D newestPoint = line.getNewest();

                dataSeriesX = ((long) latestPoint.x) + "," + ((long) newestPoint.x) + ","
                        + dataSeriesX;
                dataSeriesY = convertToShortString(latestPoint.y) + ","
                        + convertToShortString(newestPoint.y) + "," + dataSeriesY;
            }
            dataSeriesX = dataSeriesX.substring(0, dataSeriesX.length() - 1);
            dataSeriesY = dataSeriesY.substring(0, dataSeriesY.length() - 1);

            dataSeries += dataSeriesX + "|" + dataSeriesY + "|";
            dataSeriesX = "";
            dataSeriesY = "";
        }

        if (dataSeries == "") {
            Logger.w(LOG_TAG, "empty line history, aborting store graph");
            return;
        }

        String chartRequest = "chs=900x330&cht=lxy&chdl=x|y|z&chco=FF0000,00FF00,0000FF"
                + "&chg=0,50,1,0&chd=t:" + dataSeries.substring(0, dataSeries.length() - 1);
        float overallHistroyLenght = (mLineFeatureHistories[1].getFirst().getNewest().x);
        String minMax = "0," + overallHistroyLenght + ",-30,30";
        chartRequest += "&chds=" + minMax + "," + minMax + "," + minMax;

        chartRequest += "&chxt=x,y&chxr=0,0," + overallHistroyLenght + "|1,-30,30";

        String wholeRequest = "http://chart.apis.google.com/chart?" + chartRequest;

        Logger.v(LOG_TAG, "storing google chart ", wholeRequest, " to ", pFile.getAbsolutePath());

        HttpHelper.fetchUriToFile(wholeRequest.replace("|", "%7C"), pFile.getAbsolutePath());

    }

    private String convertToShortString(float pValue) {

        return Double.toString(Math.round(pValue * 10) / 10.0);
    }

    public List<LineFeature> getLineFeaturesOnAxis(int pAxis) {
        return mLineFeatureHistories[pAxis];
    }

    public void logHistory() {

        for (int i = 0; i < 3; i++) {
            float impactSum = 0;

            Logger.v(LOG_TAG, "---- on axis ", i, " remember: newest line is comming first");
            for (LineFeature feature : mLineFeatureHistories[i]) {
                impactSum += feature.getImpact();
                Logger.v(LOG_TAG, feature);
            }
            Logger.v(LOG_TAG, "overall impact is: ", impactSum);
        }

        Logger.v(LOG_TAG, "--------------------------------------");
    }
    */

}

package com.hoccer.xo.android.gesture;

import java.util.Iterator;
import java.util.LinkedList;

public class LineFeature {

    LinkedList<Vec2D> mPoints = new LinkedList<Vec2D>();

    public LineFeature(Vec2D pFirstPoint) {
        add(pFirstPoint);
    }

    public LineFeature(Vec2D firstPoint, Vec2D secondPoint) {
        this(firstPoint);
        add(secondPoint);
    }

    public boolean isValid(Vec2D pPoint) {
        if (mPoints.size() < 2) {
            return true;
        }

        return Math.abs(getYValue(pPoint.x) - pPoint.y) < 2f;
    }

    public boolean add(Vec2D pPoint) {

        if (isValid(pPoint)) {
            compress();
            mPoints.add(pPoint);
            return true;
        }

        return false;
    }

    private void compress() {
        if (mPoints.size() < 20)
            return;

        LinkedList<Vec2D> compressedPoints = new LinkedList<Vec2D>();
        int i = 0;
        for (Vec2D point : mPoints) {
            if (i % 3 == 0) {
                compressedPoints.add(point);
            }
            i++;
        }

        mPoints = compressedPoints;

    }

    public float getSlope() {

        if (mPoints.size() < 2) {
            return 0f;
        }

        float slopeSum = 0;
        Iterator<Vec2D> it = mPoints.iterator();
        Vec2D pointA = it.next();
        while (it.hasNext()) {
            Vec2D pointB = it.next();
            slopeSum += computeSlope(pointA, pointB);

            pointA = pointB;
        }
        return slopeSum / (mPoints.size() - 1);
    }

    public float getLength() {
        if (mPoints.size() < 2) {
            return 0f;
        }
        return getNewest().x - getLatest().x;
    }

    private float computeSlope(Vec2D pA, Vec2D pB) {
        return (pB.y - pA.y) / (pB.x - pA.x);
    }

    public Vec2D getCenter() {
        float avarageY = 0;
        for (Vec2D point : mPoints) {
            avarageY += point.y;
        }
        avarageY /= mPoints.size();

        return new Vec2D(mPoints.getFirst().x + getLength() / 2, avarageY);
    }

    @Override
    public String toString() {
        return "Line from " + getLatest() + " to " + getNewest()/*
                                                                 * + " with slope " + getSlope() *
                                                                 * 20 + " and length of " +
                                                                 * getLength()
                                                                 */;
    }

    public boolean isFlat() {
        if (Math.abs(getSlope()) < 0.5 / 20) {
            return true;
        }

        return false;
    }

    private float yIntersection() {
        Vec2D center = getCenter();

        return center.y - getSlope() * center.x;
    }

    private float getYValue(float x) {

        return getSlope() * x + yIntersection();
    }

    public boolean isAscending() {
        if (getSlope() >= 0.5 / 20) {
            return true;
        }

        return false;
    }

    public boolean isFastAscending() {
        if (getSlope() >= 2f / 20) {
            return true;
        }

        return false;
    }

    public boolean isDescending() {
        if (getSlope() <= -0.5 / 20) {
            return true;
        }

        return false;
    }

    public boolean isFastDescending() {
        if (getSlope() <= -2f / 20) {
            return true;
        }

        return false;
    }

    public Vec2D getLatest() {
        return mPoints.getFirst();
    }

    public Vec2D getNewest() {
        return mPoints.getLast();
    }

    public float getImpact() {
        return Math.abs(getLatest().y - getNewest().y);
    }

    public Vec2D getPointAtX(float x) {
        return new Vec2D(x, getYValue(x));
    }

}

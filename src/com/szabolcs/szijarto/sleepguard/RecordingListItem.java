package com.szabolcs.szijarto.sleepguard;

import java.util.Date;

public class RecordingListItem {

    private String datFileName;
    private String displayName;

    private Date timeStarted;
    private Date timeStopped;

    private float totalDurationInHours = 0;
    private int numberOfPeaks=0;
    private long totalDurationOfPeaksInMinutes = 0;
    private short maximumHeartRateDuringPeaks = 0;
    private int weightedHourlyPeakScore=0;

    public RecordingListItem(Recording r, RecordingFile rf) {
        datFileName = rf.getDatFileName();
        displayName = r.getDisplayName();
        timeStarted = r.getTimeStarted();
        timeStopped = r.getTimeStopped();
        totalDurationInHours = r.getTotalDurationInHours();
        numberOfPeaks = r.getNumberOfPeaks();
        totalDurationOfPeaksInMinutes = r.getTotalDurationOfPeaksInMinutes();
        maximumHeartRateDuringPeaks = r.getMaximumHeartRateDuringPeaks();
        weightedHourlyPeakScore = r.getweightedHourlyPeakScore();
    }

    public String getDatFileName() {
        return datFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getTotalDurationInHours() {
        return totalDurationInHours;
    }

    public int getNumberOfPeaks() {
        return numberOfPeaks;
    }

    public long getTotalDurationOfPeaksInMinutes() {
        return totalDurationOfPeaksInMinutes;
    }

    public short getMaximumHeartRateDuringPeaks() {
        return maximumHeartRateDuringPeaks;
    }

    public float getWeightedHourlyPeakScore() {
        return weightedHourlyPeakScore;
    }

    public Date getTimeStarted() {
        return timeStarted;
    }

    public Date getTimeStopped() {
        return timeStopped;
    }

}

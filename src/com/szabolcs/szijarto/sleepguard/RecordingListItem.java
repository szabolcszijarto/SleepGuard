package com.szabolcs.szijarto.sleepguard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingListItem {
    private static final String TAG = "RecordingListItem";

    private String datFileName;
    private String csvFileName;
    private String pngFileName;

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
        csvFileName = rf.getCsvFileName();
        pngFileName = rf.getPngFileName();
        displayName = r.getDisplayName();
        timeStarted = r.getTimeStarted();
        timeStopped = r.getTimeStopped();
        totalDurationInHours = r.getTotalDurationInHours();
        numberOfPeaks = r.getNumberOfPeaks();
        totalDurationOfPeaksInMinutes = r.getTotalDurationOfPeaksInMinutes();
        maximumHeartRateDuringPeaks = r.getMaximumHeartRateDuringPeaks();
        weightedHourlyPeakScore = r.getweightedHourlyPeakScore();
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getFormattedAttributes() {
        List<String> childItems = new ArrayList<String>();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        childItems.add( "Started:  " + ft.format(getTimeStarted()) );
        childItems.add( "Finished: " + ft.format(getTimeStopped()) );
        childItems.add( "Duration: " + getTotalDurationInHours() + " hours");
        childItems.add( "Total " + getNumberOfPeaks() + " peaks lasting " + getTotalDurationOfPeaksInMinutes() + " minutes");
        childItems.add( "Max peak BPM: " + getMaximumHeartRateDuringPeaks());
        int np = getNumberOfPeaks();
        float td = getTotalDurationInHours();
        if (td > 0) {
            childItems.add( "Average peaks per hour: " + (np/td) );
        } else {
            childItems.add( "Average peaks per hour: " + "n/a" );
        }
        childItems.add( "Weighted hourly peak score: " + getWeightedHourlyPeakScore() );
        childItems.add( "Data file: " + getDatFileName());
        childItems.add( "CSV file: " + getCsvFileName());
        childItems.add( "PNG file: " + getPngFileName());

        return childItems;
    }

    public String getDatFileName() {
        return datFileName;
    }
    public String getCsvFileName() {
        return csvFileName;
    }
    public String getPngFileName() {
        return pngFileName;
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

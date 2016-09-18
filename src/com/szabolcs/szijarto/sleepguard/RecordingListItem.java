package com.szabolcs.szijarto.sleepguard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingListItem {
    private static final String TAG = "RecordingListItem";

    private String datFileName, csvFileName, pngFileName;
    private String datFullPath, csvFullPath, pngFullPath;
    private String displayName;
    private Date timeStarted;
    private Date timeStopped;
    private float totalDurationInHours = 0;
    private int numberOfPeaks=0;
    private long totalDurationOfPeaksInMinutes = 0;
    private short maximumHeartRateDuringPeaks = 0;
    private float weightedHourlyPeakScore=0;

    public RecordingListItem(Recording rec, RecordingFile rf) {
        datFileName = rf.getDatFileName();
        csvFileName = rf.getCsvFileName();
        pngFileName = rf.getPngFileName();
        datFullPath = rf.getDatFullPath();
        csvFullPath = rf.getCsvFullPath();
        pngFullPath = rf.getPngFullPath();
        displayName = rec.getDisplayName();
        timeStarted = rec.getTimeStarted();
        timeStopped = rec.getTimeStopped();
        totalDurationInHours = rec.getTotalDurationInHours();
        numberOfPeaks = rec.getNumberOfPeaks();
        totalDurationOfPeaksInMinutes = rec.getTotalDurationOfPeaksInMinutes();
        maximumHeartRateDuringPeaks = rec.getMaximumHeartRateDuringPeaks();
        weightedHourlyPeakScore = rec.getweightedHourlyPeakScore();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String s) {
        displayName = s;
    }

    public String getLongDisplayName() {
        int hours = getTotalDurationHours();
        int mins = getTotalDurationMins();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        String s = displayName +" "+ ft.format(getTimeStopped()) +" ("+ String.format(Locale.US, "%02d:%02d", hours, mins) +")" ;

        return s;
    }

    public List<String> getFormattedAttributes() {
        List<String> childItems = new ArrayList<String>();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        childItems.add( "Started:  " + ft.format(getTimeStarted()) );
        childItems.add( "Finished: " + ft.format(getTimeStopped()) );
        childItems.add( "Duration: " + getTotalDurationHours() + " hours " + getTotalDurationMins() + " minutes");
        childItems.add( "Total " + getNumberOfPeaks() + " peaks lasting " + getTotalDurationOfPeaksInMinutes() + " minutes");
        childItems.add( "Max peak BPM: " + getMaximumHeartRateDuringPeaks());
        int np = getNumberOfPeaks();
        float td = getTotalDurationInHours();
        if (td > 0) {
            childItems.add( "Average peaks per hour: " + new DecimalFormat("##.#").format(np/td) );
        } else {
            childItems.add( "Average peaks per hour: " + "n/a" );
        }
        childItems.add( "Weighted hourly peak score: " + getWeightedHourlyPeakScore() );
        childItems.add( "Files:\n" + datFullPath + "\n" + csvFullPath + "\n" + pngFullPath);

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
    public String getDatFullPath() {
        return datFullPath;
    }
    public String getCsvFullPath() {
        return csvFullPath;
    }
    public String getPngFullPath() {
        return pngFullPath;
    }

    public float getTotalDurationInHours() {
        return totalDurationInHours;
    }
    public int getTotalDurationHours() { return (int) Math.floor(totalDurationInHours); };
    public int getTotalDurationMins() { return (int) Math.floor((totalDurationInHours - Math.floor(totalDurationInHours)) * 60); };

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

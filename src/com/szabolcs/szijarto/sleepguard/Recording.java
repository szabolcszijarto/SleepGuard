package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import android.graphics.Bitmap;

public class Recording implements java.io.Serializable {
	private static final long serialVersionUID = 19741002L;
	private transient SleepChart chart;

	private LinkedList<HeartRateRec> heartRates;
	private LinkedList<Peak> peaks;

	private static final byte TRESHOLD = 65;
	private long totalDurationOfPeaksMs;
	private short maximumHeartRateDuringPeaks;
	private float totalPeakScore;
	
	private HeartRateRec lastRecord;
	private boolean lastRecordWasAddedToTheList = false;

	private String displayName;

	public static final String nameNumericSuffix = "001";

	public Recording() {
		heartRates = new LinkedList<HeartRateRec>();
		peaks = new LinkedList<Peak>();
		displayName = "Untitled_" + nameNumericSuffix; // TODO add string resource
	}

	private boolean rateIsSameAsLast(HeartRateRec r){
		return ( r.pulse == lastRecord.pulse );
	}
	
	public void add(HeartRateRec r) {
		// we add the new record to the list only if the pulse is different from the last one (interested only in changes),  or if the list is still empty
		if ( heartRates.isEmpty() || !rateIsSameAsLast(r) ) {
			if ( !lastRecordWasAddedToTheList )
				heartRates.add(lastRecord);                // add last record of the old pulse value in order to have complete time intervals
			heartRates.add(r);
			lastRecord = r;
			lastRecordWasAddedToTheList = true;
		} else {
			lastRecord = r;								// otherwise we save this record for future use, see above
			lastRecordWasAddedToTheList = false;
		}
	}

	public void detectPeaks() {
		Boolean inpeak = false;
		Peak p = null;
		HeartRateRec r;

		peaks.clear();
		int i = 1;
		while ( i<heartRates.size() ) {
			r=heartRates.get(i);
			if ( !inpeak && (r.pulse >= TRESHOLD) ) {
				// start peak and continue
				inpeak = true;
				p = new Peak(r, i);
				if (i == 1) p.beginner = true;
			}
			else if ( inpeak && (r.pulse >= TRESHOLD) ) {
				// within peak
				p.add(r, i);
			}
			else if ( inpeak && (r.pulse < TRESHOLD) ) {
				// end of peak, save it and continue
				inpeak = false;
				p.close();
				// add it to the list only if it was not a dummy peak in the beginning ("go to bed" noise)
				if (!p.beginner) peaks.add(p);
			}
			i++;
		}

		// if still in peak at the end, close it seamlessly but do not add it to the list ("wakeup" noise)
		if (inpeak) {
			inpeak = false;
			p.close();
		}

		// calculate total duration of peaks, the maximum heart rate during peaks, and the total peak score
		totalDurationOfPeaksMs = 0;
		maximumHeartRateDuringPeaks = 0;
		totalPeakScore = 0;
		for ( Peak q : peaks ) {
			totalDurationOfPeaksMs += q.duration ;
			if ( q.max_pulse > maximumHeartRateDuringPeaks ) maximumHeartRateDuringPeaks = q.max_pulse ;
			totalPeakScore += q.score;
		}
	}
	
	public void drawChartBitmap () {
		if (chart == null) {
			chart = new SleepChart(this);
		} else {
			chart.setHrList(heartRates);
			chart.setPeakList(peaks);
			chart.draw();
		}
	}

	public Bitmap getChartBitmap() {
		if (chart == null) drawChartBitmap();
		return chart.getBitmap();
	}

	public Bitmap getChartBitmap(Peak p) {
		// construct a new HeartRateRec list with only the range of records around this particular peak
		LinkedList<HeartRateRec> hrl = new LinkedList<HeartRateRec>();
		int i, i1, i2;
		final int margin = 20; //	so many records before and after the peak will still be included
		i1 = Math.max( (p.start_index-margin), 0);
		i2 = Math.min( (p.end_index+margin), heartRates.size()-1);
		for (i=i1; i<i2; i++) {
			hrl.add(heartRates.get(i));
		}
		// construct a new Peak list containing only this particular peak 
		LinkedList<Peak> peaklist = new LinkedList<Peak>();
		peaklist.add(p);
		// draw a new chart using the data above
		SleepChart c = new SleepChart(this);
		c.setHrList(hrl);
		c.setPeakList(peaklist);
		c.draw();
		// return the chart image
		return c.getBitmap();
	}
	
	public void dumpToCsv ( BufferedWriter w ) throws IOException {
		// file size will be approx. 2775 bytes / min, which is ~ 166KB / hour or ~1.3MB per 8 hours sleep

		w.write("seqno;timestamp;pulse;heartbeats\n"); // debug only
		SimpleDateFormat ft = new SimpleDateFormat ("MM-dd HH:mm:ss.SSS", Locale.US);
		HeartRateRec r;

		for (int i=1; i<heartRates.size(); i++) {
			r = heartRates.get(i);
			w.write(i+";"+ft.format(r.timestamp)+";"+r.pulse+";"+r.heartbeats+"\n");
		}

	}

	public void dumpToPng ( FileOutputStream f ) throws IOException {
		if (chart != null ) {
			chart.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, f);
		}
	}

	public LinkedList<HeartRateRec> getHrLst() {
		return heartRates;
	}

	public LinkedList<Peak> getPeaks() {
		return peaks;
	}

	public short getTreshold() {
		return TRESHOLD;
	}

	public float getweightedHourlyPeakScore() {
		return (float) Math.floor( totalPeakScore / getTotalDurationInHours() );
	}

	public Date getTimeStarted() {
		HeartRateRec hrr = heartRates.get(1);
		Date d = hrr.timestamp;
		return d;
	}

	public Date getTimeStopped() {
		int i = heartRates.size();
		HeartRateRec hrr = heartRates.get(i-1);
		Date d = hrr.timestamp;
		return d;
	}

	public float getTotalDurationInMinutes() {
		long dms = getTimeStopped().getTime() - getTimeStarted().getTime();
		float f = (float) (dms / 1000 / 60) ;
		return f;
	}

	public float getTotalDurationInHours() {
		return ( getTotalDurationInMinutes() / 60);
	}

	public int getNumberOfPeaks() {
		return peaks.size();
	}

	public long getTotalDurationOfPeaksInMinutes() {
		return totalDurationOfPeaksMs / 1000 / 60;
	}

	public short getMaximumHeartRateDuringPeaks() {
		return maximumHeartRateDuringPeaks;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}

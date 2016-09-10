package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import android.graphics.Bitmap;

public class Recording implements java.io.Serializable {
	private static final long serialVersionUID = 19741002L;
	private transient SleepChart chart;

	private LinkedList<HeartRateRec> heartRates;
	private LinkedList<Peak> peaks;

	private static final byte TRESHOLD = 70;
	private long totalDurationOfPeaksMs = 0;
	private short maximumHeartRateDuringPeaks = 0;
	
	private HeartRateRec lastRecord;
	private boolean lastRecordWasAddedToTheList = false;

	public Recording() {
		heartRates = new LinkedList<HeartRateRec>();
		peaks = new LinkedList<Peak>();
	}

	private boolean rateIsSameAsLast(HeartRateRec r){
		return r.pulse == lastRecord.pulse;
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
		// TODO skip peak right at the beginning...
		HeartRateRec r;
		int i = 1;
		while ( i<heartRates.size() ) {
			r=heartRates.get(i);
			if ( !inpeak && (r.pulse >= TRESHOLD) ) {
				// start peak and continue
				inpeak = true;
				p = new Peak(r, i);
			}
			else if ( inpeak && (r.pulse >= TRESHOLD) ) {
				// within peak
				p.add(r, i);
			}
			else if ( inpeak && (r.pulse < TRESHOLD) ) {
				// end of peak, save it and continue
				inpeak = false;
				p.close();
				peaks.add(p);
			}
			i++;
		}

		// if still in peak at the end, close it seamlessly
		if (inpeak) {
			// within peak
			inpeak = false;
			p.close();
			peaks.add(p);
		}

		// calculate total duration of peaks and maximum heart rate
		totalDurationOfPeaksMs = 0;
		maximumHeartRateDuringPeaks = 0;
		ListIterator<Peak> j = peaks.listIterator();
		while (j.hasNext()) {
			p = j.next();
			if ( p.max_pulse > maximumHeartRateDuringPeaks ) maximumHeartRateDuringPeaks = p.max_pulse ;
			totalDurationOfPeaksMs = totalDurationOfPeaksMs + p.duration ;
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

	public int getPeaks_cnt() {
		return peaks.size();
	}

	public long getPeaks_dur() {
		return totalDurationOfPeaksMs;
	}

	public short getPeaks_max() {
		return maximumHeartRateDuringPeaks;
	}
}

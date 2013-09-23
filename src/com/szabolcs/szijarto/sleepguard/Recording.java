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

	private LinkedList<HeartRateRec> hrlst = new LinkedList<HeartRateRec>();
	private LinkedList<Peak> peaks = new LinkedList<Peak>();

	private final short treshold = 75;	// peak treshold
	private long peaks_dur = 0;			// total duration of peaks
	private short peaks_max = 0;		// max heart rate during peaks
	
	private short last_pulse = 0;
	private HeartRateRec last_rec = null;
	
	public void init() {
		last_pulse = 0;
		last_rec = null;
	}
	
	public void add(HeartRateRec r) {
		// we only add the new record to the list if the pulse is different from the last one (interested only in changes)
		if ( (r.pulse != last_pulse) || hrlst.isEmpty()) {
			if ( last_rec != null ) {
				hrlst.add(last_rec);							// add last record of the old pulse value in order to have complete time intervals
				last_rec = null;							// forget this record, no longer needed
			}
			if (hrlst.add(r)) { last_pulse = r.pulse; } ;		// add new record, and remember its pulse value if successfully added
		} else {
			last_rec = r;									// otherwise we save this record for future use, see above
		}
	}

	public void detectPeaks() {
		ListIterator<HeartRateRec> i = hrlst.listIterator();
		HeartRateRec r;
		Boolean inpeak = false;
		Peak p = null;
		// TODO skip peak right at the beginning...
		while (i.hasNext()) {
			r=i.next();
			if ( (!inpeak) && (r.pulse >= treshold) ) {
				// start peak and continue
				inpeak = true;
				p = new Peak(r, i.previousIndex());
				continue;
			}
			if ( (inpeak) && (r.pulse < treshold) ) {
				// end of peak, save it and continue
				inpeak = false;
				p.close(i.previousIndex()-1);	// -1 because the current value isn't part of the peak any more
				peaks.add(p); 					// add peak to list
				continue;
			}
			if (inpeak) {
				// within peak
				p.add(r);
			}
		}
		// if still in peak, forget about it... we need no peak at the end of a recording
		;

		// calculate total duration of peaks and maximum heart rate
		peaks_dur = 0;
		peaks_max = 0;
		ListIterator<Peak> j = peaks.listIterator();
		while (j.hasNext()) {
			p=j.next();
			if ( p.max_pulse > peaks_max ) peaks_max = p.max_pulse ;
			peaks_dur = peaks_dur + p.duration ;
		}
	}
	
	public void drawChartBitmap () {
		if (chart == null) {
			chart = new SleepChart(this);
		} else {
			chart.setHrList(hrlst);
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
		i2 = Math.min( (p.end_index+margin), hrlst.size()-1);
		for (i=i1; i<i2; i++) {
			hrl.add(hrlst.get(i));
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
		HeartRateRec r = null;
		Iterator<HeartRateRec> i = hrlst.iterator();
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
		w.write("seqno;timestamp;pulse;heartbeats\n"); // debug only
		while ( i.hasNext() ) {
			r = i.next();
			w.write(r.seqno+";"+ft.format(r.timestamp)+";"+r.pulse+";"+r.heartbeats+"\n");
		}
	}
	
	public void dumpToPng ( FileOutputStream f ) throws IOException {
		if (chart != null ) {
			chart.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, f);
		};
	}

	public LinkedList<HeartRateRec> getHrLst() {
		return hrlst;
	}

	public LinkedList<Peak> getPeaks() {
		return peaks;
	}

	public short getTreshold() {
		return treshold;
	}

	public int getPeaks_cnt() {
		return peaks.size();
	}

	public long getPeaks_dur() {
		return peaks_dur;
	}

	public short getPeaks_max() {
		return peaks_max;
	}
}

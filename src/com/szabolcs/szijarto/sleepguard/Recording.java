package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import android.graphics.Bitmap;

public class Recording implements java.io.Serializable {
	private static final long serialVersionUID = 19741001L;
	private transient SleepChart chart;
	
	LinkedList<HeartRateRec> lst = new LinkedList<HeartRateRec>();
	LinkedList<Peak> peaks = new LinkedList<Peak>();
	private short last_pulse = 0;
	HeartRateRec last_rec = null;
	
	public void init() {
		last_pulse = 0;
		last_rec = null;
	}
	
	public void add(HeartRateRec r) {
		// we only add the new record to the list if the pulse is different from the last one (interested only in changes)
		if ( (r.pulse != last_pulse) || lst.isEmpty()) {
			if ( last_rec != null ) {
				lst.add(last_rec);							// add last record of the old pulse value in order to have complete time intervals
				last_rec = null;							// forget this record, no longer needed
			}
			if (lst.add(r)) { last_pulse = r.pulse; } ;		// add new record, and remember its pulse value if successfully added
		} else {
			last_rec = r;									// otherwise we save this record for future use, see above
		}
	}

	public void detectPeaks() {
		// TODO peak detection
		;
	}
	
	public void drawChartBitmap () {
		if (chart == null) {
			chart = new SleepChart(lst);
		} else {
			chart.setHRRList(lst);
		}
	}

	public Bitmap getChartBitmap() {
		if (chart != null) {
			return chart.getBitmap();
		} else return null;
	}
	
	public void dumpToCsv ( BufferedWriter w ) throws IOException {
		// file size will be approx. 2775 bytes / min, which is ~ 166KB / hour or ~1.3MB per 8 hours sleep
		HeartRateRec r = null;
		Iterator<HeartRateRec> i = lst.iterator();
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
}

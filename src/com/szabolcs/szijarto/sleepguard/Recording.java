package com.szabolcs.szijarto.sleepguard;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class Recording implements java.io.Serializable {
	private static final long serialVersionUID = 19741001L;
	private String fileName = "";
	private transient Bitmap chart;
	public static final String datExtension = ".dat";
	public static final String csvExtension = ".csv";
	public static final String pngExtension = ".png";
	public final static String EXTRA_RECFILENAME = "com.szabolcs.szijarto.sleepguard.recordingfilename";
	
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
	
	public boolean addpeak(Peak p) {
		return peaks.add(p);
	}
	
	@Override
	public String toString() {
		return fileName+datExtension;
	}
	
	public void setFileNames(Date timeStarted, Date timeStopped) {
		SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMddHHmmss", Locale.US);
		fileName = "sleepguard_" + ft.format(timeStarted) + "_" +ft.format(timeStopped);
	}
	
	public String getDatFileName() { return fileName+datExtension;	}
	public String getCsvFileName() { return fileName+csvExtension;	}
	public String getPngFileName() { return fileName+pngExtension;	}
	
	public void dumpToCsv ( BufferedWriter w ) throws IOException {
		// file size will be approx. 2775 bytes / min, which is ~ 166KB / hour or ~1.3MB per 8 hours sleep
		HeartRateRec r = null;
		Iterator<HeartRateRec> i = lst.iterator();
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
		w.write("seqno;heartbeats;pulse;timestamp\n"); // debug only
		while ( i.hasNext() ) {
			r = i.next();
			w.write(r.seqno+";"+r.heartbeats+";"+r.pulse+";"+ft.format(r.timestamp)+"\n");
		}
	}
	
	public void dumpToPng ( FileOutputStream f ) throws IOException {
		if (chart != null ) {
			chart.compress(Bitmap.CompressFormat.PNG, 100, f);
		// TODO: Error handling if chart == null?
		};
	}
	
	public void drawChartBitmap () {

		// calculate dimensions
		HeartRateRec rec_first = lst.getFirst();
		HeartRateRec rec_last = lst.getLast();
		int n = lst.lastIndexOf(rec_last); 		// the chart will only be created up to this position, in case values are still being added in parallel
		long elapsed_secs = ( rec_last.timestamp.getTime() - rec_first.timestamp.getTime() ) / 1000;
		int x_sec_per_pixel = 10;				// horizontal resolution of chart: normally 10
		if (elapsed_secs < 600) {				// but we reduce it to 1 if recording is shorter than 10 min
			x_sec_per_pixel = 1;
		}
		int chart_width = (int)Math.ceil(elapsed_secs / x_sec_per_pixel);
		int chart_height = 280;					// vertical size of the chart in BPM
		
		int x_border = 20, y_border = 15;
		int x_size = chart_width+2*x_border;
		int y_size = chart_height+2*y_border;
		int x_origo = x_border;
		int y_origo = y_size-y_border;
		int marker_size = 2;
		
		// create bitmap and draw axis
		chart = Bitmap.createBitmap( x_size, y_size, Bitmap.Config.ARGB_8888);
		
		// create canvas
		Paint p = new Paint();
		Canvas c = new Canvas (chart);
		// fill background
		p.setColor(Color.LTGRAY);
		c.drawPaint(p);
		// draw axis
		p.setColor(Color.BLACK);
		c.drawLine(x_origo-1, y_origo+1, x_origo+chart_width, y_origo+1, p);		// X axis
		c.drawLine(x_origo-1, y_origo+1, x_origo-1, y_origo-chart_height, p);		// Y axis
		// draw Y axis (BPM) markers and label
		for (int mind = 50; mind <= 250 ; mind=mind+10) {
			p.setColor(Color.BLACK);
			p.setTextSize(8);
			if (mind%50 == 0) {
				c.drawLine(x_origo-1-2*marker_size, y_origo+1-mind, x_origo-1+2*marker_size, y_origo+1-mind, p);	// Y axis long marker
				c.drawText(String.valueOf(mind), 3, y_origo+1-mind, p);
			} else {
				c.drawLine(x_origo-1-marker_size, y_origo+1-mind, x_origo-1+marker_size, y_origo+1-mind, p);		// Y axis short marker
			}
			p.setColor(Color.GRAY);
			c.drawLine(x_origo-1+2*marker_size, y_origo+1-mind, x_origo-1+chart_width, y_origo+1-mind, p);			// horizontal line
		}

		// draw chart
		int offset;
		boolean started;
		HeartRateRec r;
		ListIterator<HeartRateRec> l = lst.listIterator(0) ;
		Path t = new Path();
		started = false;
		while (l.hasNext() && (l.nextIndex()<n)) {		// stop if no more records, or at rec_last, just in case extra records were added to the list in the meantime
			r = l.next();
			offset = (int) Math.floor( (r.timestamp.getTime() - rec_first.timestamp.getTime()) /1000.0 /x_sec_per_pixel );
			if (!started) {
				t.setLastPoint(x_origo+offset, y_origo-r.pulse);
				started = true;
			}
			t.lineTo(x_origo+offset, y_origo-r.pulse);
		}
		p.setColor(Color.argb(255, 0, 200, 0)); // dark green, non-transparent
		p.setStyle(Paint.Style.STROKE);
		c.drawPath(t, p);
	}
	
	public Bitmap getChartBitmap() {
		if (chart == null) {
			drawChartBitmap();
		}
		return chart;
	}
	
}

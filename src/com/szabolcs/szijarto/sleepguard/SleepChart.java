package com.szabolcs.szijarto.sleepguard;

import java.util.LinkedList;
import java.util.ListIterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class SleepChart {
	
	private LinkedList<HeartRateRec> hrrl;
	private Bitmap b;
	
	public SleepChart(LinkedList<HeartRateRec> hrrlst) {
		hrrl = hrrlst;
		draw();
	}
	
	public void draw() {
		// calculate dimensions
		HeartRateRec rec_first = hrrl.getFirst();
		HeartRateRec rec_last = hrrl.getLast();
		int n = hrrl.lastIndexOf(rec_last); 		// the chart will only be created up to this position, in case values are still being added in parallel
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
		b = Bitmap.createBitmap( x_size, y_size, Bitmap.Config.ARGB_8888);
		
		// create canvas
		Paint p = new Paint();
		Canvas c = new Canvas (b);
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
		ListIterator<HeartRateRec> l = hrrl.listIterator(0) ;
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

	public void setHRRList(LinkedList<HeartRateRec> hrrlst) {
		hrrl = hrrlst;
	}
	
	public Bitmap getBitmap() {
		return b;
	}
}
